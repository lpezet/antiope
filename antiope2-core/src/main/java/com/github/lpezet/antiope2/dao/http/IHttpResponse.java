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
