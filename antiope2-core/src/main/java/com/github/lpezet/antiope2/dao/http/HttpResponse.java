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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.github.lpezet.antiope2.dao.ExecutionContext;

/**
 * @author Luc Pezet
 *
 */
public class HttpResponse implements IHttpResponse {
	
	private final IHttpRequest mHttpRequest;

	private String mStatusText;
	private int mStatusCode;
	private InputStream mContent;
	private Map<String, String> mHeader = new HashMap<String, String>();
	private ExecutionContext mExecutionContext;

	/**
	 * Constructs a new HttpResponse associated with the specified request.
	 * 
	 * @param request
	 *            The associated request that generated this response.
	 * @param httpRequest
	 *            The underlying http request that generated this response.
	 */
	public HttpResponse(IHttpRequest pRequest) {
		mHttpRequest = pRequest;
	}

	/**
	 * Returns the original request associated with this response.
	 * 
	 * @return The original request associated with this response.
	 */
	// public Request<?> getRequest() {
	// return request;
	// }

	/**
	 * Returns the original http request associated with this response.
	 * 
	 * @return The original http request associated with this response.
	 */
	public IHttpRequest getHttpRequest() {
		return mHttpRequest;
	}

	/**
	 * Returns the HTTP headers returned with this response.
	 * 
	 * @return The set of HTTP headers returned with this HTTP response.
	 */
	public Map<String, String> getHeaders() {
		return mHeader;
	}

	/**
	 * Adds an HTTP header to the set associated with this response.
	 * 
	 * @param name
	 *            The name of the HTTP header.
	 * @param value
	 *            The value of the HTTP header.
	 */
	public void addHeader(String name, String value) {
		mHeader.put(name, value);
	}

	/**
	 * Sets the input stream containing the response content.
	 * 
	 * @param content
	 *            The input stream containing the response content.
	 */
	public void setContent(InputStream content) {
		this.mContent = content;
	}

	/**
	 * Returns the input stream containing the response content.
	 * 
	 * @return The input stream containing the response content.
	 */
	public InputStream getContent() {
		return mContent;
	}

	/**
	 * Sets the HTTP status text returned with this response.
	 * 
	 * @param statusText
	 *            The HTTP status text (ex: "Not found") returned with this
	 *            response.
	 */
	public void setStatusText(String statusText) {
		this.mStatusText = statusText;
	}

	/**
	 * Returns the HTTP status text associated with this response.
	 * 
	 * @return The HTTP status text associated with this response.
	 */
	public String getStatusText() {
		return mStatusText;
	}

	/**
	 * Sets the HTTP status code that was returned with this response.
	 * 
	 * @param statusCode
	 *            The HTTP status code (ex: 200, 404, etc) associated with this
	 *            response.
	 */
	public void setStatusCode(int statusCode) {
		this.mStatusCode = statusCode;
	}

	/**
	 * Returns the HTTP status code (ex: 200, 404, etc) associated with this
	 * response.
	 * 
	 * @return The HTTP status code associated with this response.
	 */
	public int getStatusCode() {
		return mStatusCode;
	}

	@Override
	public void setHeaders(Map<String, String> pHeaders) {
		mHeader = pHeaders;
	}

	@Override
	public ExecutionContext getExecutionContext() {
		return mExecutionContext;
	}

	@Override
	public void setExecutionContext(ExecutionContext pContext) {
		if (pContext != null) mExecutionContext = pContext;
	}
	
	@Override
	public void close() {
	}

}
