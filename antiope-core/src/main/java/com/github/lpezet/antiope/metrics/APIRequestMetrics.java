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
package com.github.lpezet.antiope.metrics;


/**
 * 
 */
// NotThreadSafe
public enum APIRequestMetrics implements RequestMetricType {
	APIErrorCode,
	APIRequestID,
	BytesProcessed,
	/**
	 * Total number of milliseconds taken for a request/response including
	 * the time taken to execute the request handlers, round trip to API,
	 * and the time taken to execute the response handlers.
	 */
	ClientExecuteTime,
	CredentialsRequestTime,
	Exception,
	/**
	 * Number of milliseconds taken for a request/response round trip to API.
	 */
	HttpRequestTime,
	RedirectLocation,
	RequestMarshallTime,
	/**
	 * Number of milliseconds taken to sign a request.
	 */
	RequestSigningTime,
	/**
	 * Number of milliseconds taken to execute the response handler for a response from API.
	 */
	ResponseProcessingTime,
	/**
	 * Number of requests to API.
	 */
	RequestCount,
	/**
	 * Number of retries of API SDK sending a request to API.
	 */
	RetryCount, // captured via the RequestCount since (RetryCount = RequestCount - 1)
	/**
	 * Number of retries of the underlying http client library in sending a
	 * request to API.
	 */
	HttpClientRetryCount,
	/**
	 * Time taken to send a request to API by the http client library,
	 * excluding any retry.
	 */
	HttpClientSendRequestTime,
	/**
	 * Time taken to receive a response from API by the http client library,
	 * excluding any retry.
	 */
	HttpClientReceiveResponseTime,
	RetryPauseTime,
	ServiceEndpoint,
	ServiceName,
	StatusCode, // The http status code
	;
}
