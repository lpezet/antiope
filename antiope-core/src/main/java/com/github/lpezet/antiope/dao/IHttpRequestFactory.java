/**
 * 
 */
package com.github.lpezet.antiope.dao;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HttpContext;

import com.github.lpezet.antiope.be.APIConfiguration;

/**
 * @author luc
 *
 */
public interface IHttpRequestFactory {

	public HttpRequestBase createHttpRequest(Request<?> pRequest, APIConfiguration pConfiguration, HttpContext pHttpContext, ExecutionContext pContext);
	
}
