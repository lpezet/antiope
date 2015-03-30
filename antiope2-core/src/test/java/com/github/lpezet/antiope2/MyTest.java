/**
 * 
 */
package com.github.lpezet.antiope2;

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
import com.github.lpezet.antiope2.dao.http.apache.ApacheHttpClient;
import com.github.lpezet.antiope2.dao.http.apache.ApacheHttpClientMarshaller;
import com.github.lpezet.antiope2.dao.http.apache.ApacheHttpClientUnmarshaller;
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
		ApacheHttpClient oHttpClient = new ApacheHttpClient(pHttpClient);
		ApacheHttpClientMarshaller oMarshaller = new ApacheHttpClientMarshaller();
		ApacheHttpClientUnmarshaller oUnmarshaller = new ApacheHttpClientUnmarshaller();
		ApacheHttpClientNetworkIO oIO = new ApacheHttpClientNetworkIO(oMarshaller, oHttpClient, oUnmarshaller);
		return oIO;
	}

	@Test
	public void simple() throws Exception {
		MyClient oClient = newMyClient( buildHttpClient() );
		
		for (int i = 0; i < 100; i++) {
			MyRequest oRequest = new MyRequest();
			oRequest.setIP("75.179.140.18");
			MyResponse oResponse = oClient.ask(oRequest);
			assertNotNull(oResponse);
			assertNotNull(oResponse.getContent());
			//System.out.println("Content:\n" + oResponse.getContent());
			System.out.println("####### " + i + " done.");
		}
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
		oClient.ask(oRequest);
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
