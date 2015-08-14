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
package com.github.lpezet.antiope2.dao.http.apache;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lpezet.antiope2.dao.http.HttpResponse;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;

/**
 * @author Luc Pezet
 *
 */
public class ApacheHttpClientUnmarshaller implements IApacheHttpClientUnmarshaller {
	
	private static class HttpResponseWrapper extends HttpResponse {
		
		private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponseWrapper.class);
		
		private org.apache.http.HttpResponse mHttpResponse;

		public HttpResponseWrapper(IHttpRequest pRequest, org.apache.http.HttpResponse pHttpResponse) {
			super(pRequest);
			mHttpResponse = pHttpResponse;
		}
		
		@Override
		public void close() {
			if (mHttpResponse != null && mHttpResponse instanceof CloseableHttpResponse) {
				CloseableHttpResponse oCloseable = (CloseableHttpResponse) mHttpResponse;
				try {
					oCloseable.close();
				} catch (IOException e) {
					if (LOGGER.isWarnEnabled()) LOGGER.warn("Exception while closing Apache HttpResponse (might be benine).", e);
				}
			}
		}
		
	}
	
	@Override
	public IHttpResponse perform(ApacheHttpResponse pResponse) throws Exception {
		HttpResponse oResponse = new HttpResponseWrapper(pResponse.getRequest(), pResponse.getHttpResponse());
		oResponse.setExecutionContext(pResponse.getExecutionContext());
		org.apache.http.HttpResponse oApacheHttpResponse = pResponse.getHttpResponse();
		if (oApacheHttpResponse != null) {
			if (oApacheHttpResponse.getEntity() != null) {
				oResponse.setContent(oApacheHttpResponse.getEntity().getContent());
			}
	
			oResponse.setStatusCode(oApacheHttpResponse.getStatusLine().getStatusCode());
			oResponse.setStatusText(oApacheHttpResponse.getStatusLine().getReasonPhrase());
			for (Header header : oApacheHttpResponse.getAllHeaders()) {
				oResponse.addHeader(header.getName(), header.getValue());
			}
		}

		return oResponse;
		
	}

}
