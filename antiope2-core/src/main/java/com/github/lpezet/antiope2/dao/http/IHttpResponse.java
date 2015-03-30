/**
 * 
 */
package com.github.lpezet.antiope2.dao.http;

import com.github.lpezet.antiope2.dao.IExecutionContextAware;

/**
 * @author Luc Pezet
 *
 */
public interface IHttpResponse extends IHttpBase, IExecutionContextAware {

	/**
	 * Returns the original http request associated with this response.
	 * 
	 * @return The original http request associated with this response.
	 */
	//public HttpRequestBase getHttpRequest();
	public IHttpRequest getHttpRequest();
	
	
	/**
	 * Sets the HTTP status text returned with this response.
	 * 
	 * @param statusText
	 *            The HTTP status text (ex: "Not found") returned with this
	 *            response.
	 */
	public void setStatusText(String statusText);

	/**
	 * Returns the HTTP status text associated with this response.
	 * 
	 * @return The HTTP status text associated with this response.
	 */
	public String getStatusText();

	/**
	 * Sets the HTTP status code that was returned with this response.
	 * 
	 * @param statusCode
	 *            The HTTP status code (ex: 200, 404, etc) associated with this
	 *            response.
	 */
	public void setStatusCode(int statusCode);

	/**
	 * Returns the HTTP status code (ex: 200, 404, etc) associated with this
	 * response.
	 * 
	 * @return The HTTP status code associated with this response.
	 */
	public int getStatusCode();
	
	
	/**
	 * Close underlying connection if not already closed to free up resources. 
	 */
	public void close();
}
