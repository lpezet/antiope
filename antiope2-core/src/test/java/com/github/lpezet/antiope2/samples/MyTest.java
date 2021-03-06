/**
 * The MIT License
 * Copyright (c) 2014 Luc Pezet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.lpezet.antiope2.samples;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;
import com.github.lpezet.antiope2.dao.http.apache.ApacheHttpClientNetworkIO;
import com.github.lpezet.antiope2.metrics.IMetrics;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;
import com.github.lpezet.antiope2.metrics.LogMetricsCollector;
import com.github.lpezet.antiope2.metrics.TimingInfo;

/**
 * @author Luc Pezet
 *
 */
public class MyTest {
	
	private static HttpClient buildHttpClient() throws Exception {
		SSLContextBuilder builder = new SSLContextBuilder();
	    builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
	    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
	            builder.build());
	    HttpClient oHC = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.build();
	    return oHC;
	}
	
	private static HttpClient buildHttpClientMock() throws Exception {
		return mock(HttpClient.class);
	}
	
	public MyClient newMyClient(HttpClient pHttpClient) throws Exception {
		ApacheHttpClientNetworkIO oIO = createApacheHttpClient(pHttpClient);
		LogMetricsCollector oMetricsCollector = new LogMetricsCollector();
		return new MyClient(oIO, oMetricsCollector);
	}

	private ApacheHttpClientNetworkIO createApacheHttpClient(HttpClient pHttpClient) {
		return new ApacheHttpClientNetworkIO(pHttpClient);
	}

	@Test
	public void simple() throws Exception {
		MyClient oClient = newMyClient( buildHttpClient() );
		
		MyRequest oRequest = new MyRequest();
		oRequest.setIP("75.179.140.18");
		MyResponse oResponse = oClient.ask(oRequest);
		assertNotNull(oResponse);
		assertNotNull(oResponse.getContent());
		System.out.println("Content:\n" + oResponse.getContent());
	}
	
	@Test
	public void performance() throws Exception {
		// Mocking actual Apache Http Client
		HttpClient oHttpClient = buildHttpClientMock();
		ApacheHttpClientNetworkIO oApacheIOStack = createApacheHttpClient(oHttpClient);
		PerformanceCollector oCollector = newPerformanceCollector();
		MyClient oClient = new MyClient(oApacheIOStack, oCollector);
		
		MyRequest oRequest = new MyRequest();
		oRequest.setIP("75.179.140.18");
		oClient.ask(oRequest); // warm up
		for (int i = 0; i < 1000; i++) {
			oClient.ask(oRequest);
		}
		
		
		System.out.println(oCollector.toString() );
		System.out.println("GeoMean = " + oCollector.getGeometricMean());
	}
	
	private static class PerformanceCollector implements IMetricsCollector {
		
		private DescriptiveStatistics mStats = new DescriptiveStatistics();
		private boolean mFirstPass = true;
		
		@Override
		public boolean isEnabled() {
			return true;
		}
		
		@Override
		public <RQ extends IHttpRequest, RS extends IHttpResponse> void collectMetrics(RQ pRequest, RS pResponse) {
			if (mFirstPass) {
				System.out.println("Skipping first execution.");
				mFirstPass = false;
				return;
			}
			latencyOfClientExecuteTime(pRequest, pResponse);
		}
		
		protected <RQ extends IHttpRequest> void latencyOfClientExecuteTime(RQ req, Object response) {
			IMetrics m = req.getExecutionContext().getMetrics();
	        TimingInfo root = m.getTimingInfo();
	        if (root.isEndTimeKnown()) { // being defensive
	        	mStats.addValue( root.getTimeTakenMillisIfKnown() );
	        }
	    }
		
		public long getN() {
			return mStats.getN();
		}
		
		public double getMeanTime() {
			return mStats.getMean();
		}
		
		public double getStandardDeviation() {
			return mStats.getStandardDeviation();
		}
		
		public double getPercentile(int pPercentile) {
			return mStats.getPercentile(pPercentile);
		}
		
		public double getGeometricMean() {
			return mStats.getGeometricMean();
		}
		
		@Override
		public String toString() {
			return mStats.toString();
		}
		
	}

	private PerformanceCollector newPerformanceCollector() {
		return new PerformanceCollector();
	}
}
