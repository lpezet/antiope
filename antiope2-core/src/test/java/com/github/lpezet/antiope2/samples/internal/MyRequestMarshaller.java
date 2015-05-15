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
package com.github.lpezet.antiope2.samples.internal;

import java.net.URI;
import java.net.URISyntaxException;

import com.github.lpezet.antiope2.dao.IMarshaller;
import com.github.lpezet.antiope2.dao.http.HttpExecutionContext;
import com.github.lpezet.antiope2.dao.http.HttpMethodName;
import com.github.lpezet.antiope2.dao.http.HttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.metrics.BaseMetrics;
import com.github.lpezet.antiope2.metrics.IMetrics;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;
import com.github.lpezet.antiope2.samples.MyRequest;

/**
 * @author Luc Pezet
 *
 */
public class MyRequestMarshaller implements IMarshaller<MyRequest, IHttpRequest> {
	
	private static final URI ENDPOINT = newURI("http://freegeoip.net");
	private IMetricsCollector mMetricsCollector;
	
	public MyRequestMarshaller() {
		this(null);
	}
	
	public MyRequestMarshaller(IMetricsCollector pMetricsCollector) {
		mMetricsCollector = pMetricsCollector;
	}

	@Override
	public IHttpRequest perform(MyRequest pSource) throws Exception {
		IMetrics oMetrics = new BaseMetrics();
		HttpExecutionContext oContext = new HttpExecutionContext(oMetrics, mMetricsCollector);
		
		HttpRequest oResult = new HttpRequest("MyService");
		oResult.setExecutionContext(oContext);
		oResult.setEndpoint(ENDPOINT);
		oResult.setResourcePath("/json/" + pSource.getIP());
		oResult.setHttpMethod(HttpMethodName.GET);
		//oResult.addHeader("toto", "titi");
		return oResult;
	}

	private static URI newURI(String pURI) {
		try {
			return new URI(pURI);
		} catch (URISyntaxException e) {
			return null;
		}
	}

}
