/*
 * Copyright 2011-2013 Amazon Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lpezet.antiope2.dao.http;



/**
 * @author luc
 */
public abstract class RequestHandler {

	/**
	 * Runs any additional processing logic on the specified request (before it
	 * is executed by the client runtime).
	 * 
	 * @param request
	 *            The low level request being processed.
	 */
	public abstract void beforeRequest(IHttpRequest request);

	/**
	 * Runs any additional processing logic on the specified request (after is
	 * has been executed by the client runtime).
	 * 
	 * @param request
	 *            The low level request being processed.
	 * @param response
	 *            The response generated from the specified request.
	 */
	public abstract void afterResponse(IHttpRequest request, IHttpResponse response);

	/**
	 * Runs any additional processing logic on a request after it has failed.
	 * 
	 * @param request
	 *            The request that generated an error.
	 * @param response
	 *            the response or null if the failure occurred before the
	 *            response is made available
	 * @param e
	 *            The error that resulted from executing the request.
	 */
	public abstract void afterError(IHttpRequest request, IHttpResponse response, Exception e);

}
