/**
 * 
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
