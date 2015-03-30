/**
 * 
 */
package com.github.lpezet.antiope2.dao.http.apache;

import org.apache.http.HttpResponse;

import com.github.lpezet.antiope2.dao.ExecutionContext;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;

/**
 * @author Luc Pezet
 *
 */
public class ApacheHttpResponse {

	private IHttpRequest mRequest;
	private HttpResponse mHttpResponse;
	private ExecutionContext mExecutionContext;
	
	public HttpResponse getHttpResponse() {
		return mHttpResponse;
	}
	
	public void setHttpResponse(HttpResponse pHttpResponse) {
		mHttpResponse = pHttpResponse;
	}

	public IHttpRequest getRequest() {
		return mRequest;
	}

	public void setRequest(IHttpRequest pRequest) {
		mRequest = pRequest;
	}

	public ExecutionContext getExecutionContext() {
		return mExecutionContext;
	}

	public void setExecutionContext(ExecutionContext pExecutionContext) {
		mExecutionContext = pExecutionContext;
	}
}
