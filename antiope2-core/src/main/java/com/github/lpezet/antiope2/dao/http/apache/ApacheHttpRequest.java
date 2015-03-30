/**
 * 
 */
package com.github.lpezet.antiope2.dao.http.apache;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HttpContext;

import com.github.lpezet.antiope2.dao.ExecutionContext;
import com.github.lpezet.antiope2.dao.IExecutionContextAware;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;

/**
 * @author Luc Pezet
 *
 */
public class ApacheHttpRequest implements IExecutionContextAware {

	private IHttpRequest mOriginalHttpRequest;
	private HttpRequestBase mHttpRequestBase;
	private HttpContext mHttpContext;
	private ExecutionContext mExecutionContext;

	public HttpRequestBase getHttpRequestBase() {
		return mHttpRequestBase;
	}

	public void setHttpRequestBase(HttpRequestBase pHttpRequestBase) {
		mHttpRequestBase = pHttpRequestBase;
	}

	public HttpContext getHttpContext() {
		return mHttpContext;
	}

	public void setHttpContext(HttpContext pHttpContext) {
		mHttpContext = pHttpContext;
	}

	public ExecutionContext getExecutionContext() {
		return mExecutionContext;
	}

	public void setExecutionContext(ExecutionContext pExecutionContext) {
		mExecutionContext = pExecutionContext;
	}

	public IHttpRequest getOriginalHttpRequest() {
		return mOriginalHttpRequest;
	}

	public void setOriginalHttpRequest(IHttpRequest pOriginalHttpRequest) {
		mOriginalHttpRequest = pOriginalHttpRequest;
	}

}
