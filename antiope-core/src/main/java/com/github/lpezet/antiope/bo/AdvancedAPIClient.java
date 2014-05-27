/**
 * 
 */
package com.github.lpezet.antiope.bo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lpezet.antiope.APIClientException;
import com.github.lpezet.antiope.APIServiceException;
import com.github.lpezet.antiope.APIServiceException.ErrorType;
import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.be.APIWebServiceResponse;
import com.github.lpezet.antiope.be.IAPICredentials;
import com.github.lpezet.antiope.be.IAPICredentialsProvider;
import com.github.lpezet.antiope.be.StaticCredentialsProvider;
import com.github.lpezet.antiope.dao.CRC32MismatchException;
import com.github.lpezet.antiope.dao.CountingInputStream;
import com.github.lpezet.antiope.dao.ExecutionContext;
import com.github.lpezet.antiope.dao.HttpMethodReleaseInputStream;
import com.github.lpezet.antiope.dao.HttpResponse;
import com.github.lpezet.antiope.dao.HttpResponseHandler;
import com.github.lpezet.antiope.dao.IHttpRequestFactory;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.metrics.APIRequestMetrics;
import com.github.lpezet.antiope.metrics.IMetrics;
import com.github.lpezet.antiope.transform.Unmarshaller;

/**
 * @author luc
 */
public abstract class AdvancedAPIClient<R> extends BaseAPIClient<R> {

	private static final String	HEADER_USER_AGENT	= "User-Agent";

	private Logger				mLogger				= LoggerFactory.getLogger(this.getClass());

	private IHttpRequestFactory	mHttpRequestFactory;
	private HttpClient			mHttpClient;

	public AdvancedAPIClient(APIConfiguration pConfiguration, HttpClient pHttpClient) {
		super(pConfiguration);
		mHttpClient = pHttpClient;
	}

	public AdvancedAPIClient(APIConfiguration pConfiguration, IAPICredentials pCredentials, HttpClient pHttpClient) {
		super(pConfiguration, new StaticCredentialsProvider(pCredentials));
		mHttpClient = pHttpClient;
	}

	public AdvancedAPIClient(APIConfiguration pConfiguration, IAPICredentialsProvider pCrendentialsProvider, HttpClient pHttpClient) {
		super(pConfiguration, pCrendentialsProvider);
		mHttpClient = pHttpClient;
	}

	public IHttpRequestFactory getHttpRequestFactory() {
		return mHttpRequestFactory;
	}

	public void setHttpRequestFactory(IHttpRequestFactory pHttpRequestFactory) {
		mHttpRequestFactory = pHttpRequestFactory;
	}

	protected abstract <T> HttpResponseHandler<APIWebServiceResponse<T>> createResponseHandler(Unmarshaller<T, R> pUnmarshaller);

	@Override
	protected <T> Response<T> doInvoke(Request<?> pRequest, Unmarshaller<T, R> pUnmarshaller, HttpResponseHandler<APIServiceException> pErrorResponseHandler, ExecutionContext pExecutionContext) throws APIClientException, APIServiceException {
		HttpResponseHandler<APIWebServiceResponse<T>> oResponseHandler = createResponseHandler(pUnmarshaller);
		
		boolean leaveHttpConnectionOpen = false;
		
		IMetrics oMetrics = pExecutionContext.getMetrics();
		// Add service metrics.
		oMetrics.addProperty(APIRequestMetrics.ServiceName, pRequest.getServiceName());
		oMetrics.addProperty(APIRequestMetrics.ServiceEndpoint, pRequest.getEndpoint());
		
		// Apply whatever request options we know how to handle, such as user-agent.
		setUserAgent(pRequest);
		
		int oRequestCount = 0;
		URI oRedirectedURI = null;
        HttpEntity oEntity = null;
        APIClientException oRetriedException = null;
        
        // Make a copy of the original request params and headers so that we can
        // permute it in this loop and start over with the original every time.
        Map<String, String> originalParameters = new HashMap<String, String>();
        originalParameters.putAll(pRequest.getParameters());
        Map<String, String> originalHeaders = new HashMap<String, String>();
        originalHeaders.putAll(pRequest.getHeaders());
        
        while (true) {
        	++oRequestCount;
            oMetrics.setCounter(APIRequestMetrics.RequestCount, oRequestCount);
            if (oRequestCount > 1) { // retry
                pRequest.setParameters(originalParameters);
                pRequest.setHeaders(originalHeaders);
            }
            
            HttpRequestBase oHttpRequest = null;
            org.apache.http.HttpResponse oApacheResponse = null;
            
            try {
            	if (mLogger.isDebugEnabled()) mLogger.debug("Sending Request: " + pRequest.toString());
            	
            	HttpContext oHttpContext = HttpClientContext.create();
            	//NB: Signing should happen in createHttpRequest().
            	oHttpRequest = mHttpRequestFactory.createHttpRequest(pRequest, getAPIConfiguration(), oEntity, oHttpContext, pExecutionContext);
            	
            	if (oHttpRequest instanceof HttpEntityEnclosingRequest) {
            		oEntity = ((HttpEntityEnclosingRequest) oHttpRequest).getEntity();
                }

                if (oRedirectedURI != null) {
                	oHttpRequest.setURI(oRedirectedURI);
                }
                
                //TODO
                /*
                if (oRequestCount > 1) {   // retry
                    oMetrics.startEvent(APIRequestMetrics.RetryPauseTime);
                    try {
                        pauseBeforeNextRetry(pRequest.getOriginalRequest(),
                                             oRetriedException,
                                             oRequestCount,
                                             getAPIConfiguration().getRetryPolicy());
                    } finally {
                    	oMetrics.endEvent(APIRequestMetrics.RetryPauseTime);
                    }
                }
                */
                
                if ( oEntity != null ) {
                    InputStream content = oEntity.getContent();
                    if ( oRequestCount > 1 ) {   // retry
                        if ( content.markSupported() ) {
                            content.reset();
                            content.mark(-1);
                        }
                    } else {
                        if ( content.markSupported() ) {
                            content.mark(-1);
                        }
                    }
                }
                
                oRetriedException = null;
                oMetrics.startEvent(APIRequestMetrics.HttpRequestTime);
                try {
                    oApacheResponse = mHttpClient.execute(oHttpRequest, oHttpContext);
                } finally {
                	oMetrics.endEvent(APIRequestMetrics.HttpRequestTime);
                }
                
                if (isRequestSuccessful(oApacheResponse)) {
                    oMetrics.addProperty(APIRequestMetrics.StatusCode, oApacheResponse.getStatusLine().getStatusCode());
                    /*
                     * If we get back any 2xx status code, then we know we should
                     * treat the service call as successful.
                     */
                    leaveHttpConnectionOpen = oResponseHandler.needsConnectionLeftOpen();
                    HttpResponse oHttpResponse = createResponse(oHttpRequest, pRequest, oApacheResponse);
                    T oResponse = handleResponse(pRequest, oResponseHandler, oHttpRequest, oHttpResponse, oApacheResponse, pExecutionContext);
                    return new Response<T>(oResponse, oHttpResponse);
                    
                } else if (isTemporaryRedirect(oApacheResponse)) {
                    /*
                     * S3 sends 307 Temporary Redirects if you try to delete an
                     * EU bucket from the US endpoint. If we get a 307, we'll
                     * point the HTTP method to the redirected location, and let
                     * the next retry deliver the request to the right location.
                     */
                    Header[] locationHeaders = oApacheResponse.getHeaders("location");
                    String redirectedLocation = locationHeaders[0].getValue();
                    //log.debug("Redirecting to: " + redirectedLocation);
                    if (mLogger.isInfoEnabled()) mLogger.info("Redirecting to: " + redirectedLocation);
                    oRedirectedURI = URI.create(redirectedLocation);
                    oHttpRequest.setURI(oRedirectedURI);
                    oMetrics.addProperty(APIRequestMetrics.StatusCode, oApacheResponse.getStatusLine().getStatusCode());
                    oMetrics.addProperty(APIRequestMetrics.RedirectLocation, redirectedLocation);
                    oMetrics.addProperty(APIRequestMetrics.APIRequestID, null);

                } else {
                    leaveHttpConnectionOpen = pErrorResponseHandler.needsConnectionLeftOpen();
                    APIServiceException ase = handleErrorResponse(pRequest, pErrorResponseHandler, oHttpRequest, oApacheResponse);
                    oMetrics.addProperty(APIRequestMetrics.APIRequestID, ase.getRequestId());
                    oMetrics.addProperty(APIRequestMetrics.APIErrorCode, ase.getErrorCode());
                    oMetrics.addProperty(APIRequestMetrics.StatusCode, ase.getStatusCode());
                    
                    //TODO
                    /*
                    if (!shouldRetry(pRequest.getOriginalRequest(),
                                     oHttpRequest,
                                     ase,
                                     oRequestCount,
                                     config.getRetryPolicy())) {
                        throw ase;
                    }
                    */

                    // Cache the retryable exception
                    oRetriedException = ase;
                    /*
                     * Checking for clock skew error again because we don't want to set the
                     * global time offset for every service exception.
                     */
                    /*
                    if(RetryUtils.isClockSkewError(ase)) {
                        int timeOffset = parseClockSkewOffset(apacheResponse, ase);
                        SDKGlobalConfiguration.setGlobalTimeOffset(timeOffset);
                    }
                    */
                    resetRequestAfterError(pRequest, ase);
                }
            } catch (IOException ioe) {
                if (mLogger.isInfoEnabled()) mLogger.info("Unable to execute HTTP request: " + ioe.getMessage(), ioe);
                oMetrics.incrementCounter(APIRequestMetrics.Exception);
                oMetrics.addProperty(APIRequestMetrics.Exception, ioe.toString());
                oMetrics.addProperty(APIRequestMetrics.APIRequestID, null);

                APIClientException ace = new APIClientException("Unable to execute HTTP request: " + ioe.getMessage(), ioe);
                //TODO
                /*
                if (!shouldRetry(request.getOriginalRequest(),
                                httpRequest,
                                ace,
                                requestCount,
                                config.getRetryPolicy())) {
                    throw ace;
                }
                */
                // Cache the retryable exception
                oRetriedException = ace;
                resetRequestAfterError(pRequest, ioe);
            } catch(RuntimeException e) {
                throw handleUnexpectedFailure(e, oMetrics);
            } catch(Error e) {
                throw handleUnexpectedFailure(e, oMetrics);
            } finally {
                /*
                 * Some response handlers need to manually manage the HTTP
                 * connection and will take care of releasing the connection on
                 * their own, but if this response handler doesn't need the
                 * connection left open, we go ahead and release the it to free
                 * up resources.
                 */
                if (!leaveHttpConnectionOpen) {
                    try {
                        if (oApacheResponse != null && oApacheResponse.getEntity() != null
                                && oApacheResponse.getEntity().getContent() != null) {
                        	oApacheResponse.getEntity().getContent().close();
                        }
                    } catch (IOException e) {
                        mLogger.warn("Cannot close the response content.", e);
                    }
                }
            }
        }
		
	}
	
	/**
     * Handles an unexpected failure, returning the Throwable instance as given.
     */
    private <T extends Throwable> T handleUnexpectedFailure(T t, IMetrics pMetrics) {
    	pMetrics.incrementCounter(APIRequestMetrics.Exception);
        return t;
    }
	
	/**
     * Resets the specified request, so that it can be sent again, after
     * receiving the specified error. If a problem is encountered with resetting
     * the request, then an TSGClientException is thrown with the original
     * error as the cause (not an error about being unable to reset the stream).
     *
     * @param request
     *            The request being executed that failed and needs to be reset.
     * @param cause
     *            The original error that caused the request to fail.
     *
     * @throws TSGClientException
     *             If the request can't be reset.
     */
    private void resetRequestAfterError(Request<?> request, Exception cause) throws APIClientException {
        if ( request.getContent() == null ) {
            return; // no reset needed
        }
        if ( ! request.getContent().markSupported() ) {
            throw new APIClientException("Encountered an exception and stream is not resettable", cause);
        }
        try {
            request.getContent().reset();
        } catch ( IOException e ) {
            // This exception comes from being unable to reset the input stream,
            // so throw the original, more meaningful exception
            throw new APIClientException(
                    "Encountered an exception and couldn't reset the stream to retry", cause);
        }
    }
	
	/**
     * Responsible for handling an error response, including unmarshalling the
     * error response into the most specific exception type possible, and
     * throwing the exception.
     *
     * @param request
     *            The request that generated the error response being handled.
     * @param errorResponseHandler
     *            The response handler responsible for unmarshalling the error
     *            response.
     * @param method
     *            The HTTP method containing the actual response content.
     *
     * @throws IOException
     *             If any problems are encountering reading the error response.
     */
    private APIServiceException handleErrorResponse(Request<?> request, HttpResponseHandler<APIServiceException> errorResponseHandler, HttpRequestBase method, org.apache.http.HttpResponse apacheHttpResponse) throws IOException {

        int status = apacheHttpResponse.getStatusLine().getStatusCode();
        HttpResponse response = createResponse(method, request, apacheHttpResponse);
        if (errorResponseHandler.needsConnectionLeftOpen() && method instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase entityEnclosingRequest = (HttpEntityEnclosingRequestBase)method;
            response.setContent(new HttpMethodReleaseInputStream(entityEnclosingRequest));
        }

        APIServiceException exception = null;
        try {
            exception = errorResponseHandler.handle(response);
            if (mLogger.isDebugEnabled()) mLogger.debug("Received error response: " + exception.toString());
        } catch (Exception e) {
            // If the errorResponseHandler doesn't work, then check for error
            // responses that don't have any content
            if (status == 413) {
                exception = new APIServiceException("Request entity too large");
                exception.setServiceName(request.getServiceName());
                exception.setStatusCode(413);
                exception.setErrorType(ErrorType.Client);
                exception.setErrorCode("Request entity too large");
            } else if (status == 503 && "Service Unavailable".equalsIgnoreCase(apacheHttpResponse.getStatusLine().getReasonPhrase())) {
                exception = new APIServiceException("Service unavailable");
                exception.setServiceName(request.getServiceName());
                exception.setStatusCode(503);
                exception.setErrorType(ErrorType.Service);
                exception.setErrorCode("Service unavailable");
            } else {
                String errorMessage = "Unable to unmarshall error response (" + e.getMessage() + ")";
                throw new APIClientException(errorMessage, e);
            }
        }

        exception.setStatusCode(status);
        exception.setServiceName(request.getServiceName());
        exception.fillInStackTrace();
        return exception;
    }

	/**
	 * Handles a successful response from a service call by unmarshalling the
	 * results using the specified response handler.
	 * 
	 * @param <T>
	 *            The type of object expected in the response.
	 * @param pRequest
	 *            The original request that generated the response being
	 *            handled.
	 * @param pResponseHandler
	 *            The response unmarshaller used to interpret the contents of
	 *            the response.
	 * @param pMethod
	 *            The HTTP method that was invoked, and contains the contents of
	 *            the response.
	 * @param pExecutionContext
	 *            Extra state information about the request currently being
	 *            executed.
	 * @return The contents of the response, unmarshalled using the specified
	 *         response handler.
	 * @throws IOException
	 *             If any problems were encountered reading the response
	 *             contents from the HTTP method object.
	 */
	private <T> T handleResponse(Request<?> pRequest, HttpResponseHandler<APIWebServiceResponse<T>> pResponseHandler, HttpRequestBase pMethod, HttpResponse pHttpResponse, org.apache.http.HttpResponse pApacheHttpResponse, ExecutionContext pExecutionContext) throws IOException {
		if (pResponseHandler.needsConnectionLeftOpen() && pMethod instanceof HttpEntityEnclosingRequest) {
			HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) pMethod;
			pHttpResponse.setContent(new HttpMethodReleaseInputStream(httpEntityEnclosingRequest));
		}

		try {
			CountingInputStream countingInputStream = null;
			if (getAPIConfiguration().isProfilingEnabled()) {
				countingInputStream = new CountingInputStream(pHttpResponse.getContent());
				pHttpResponse.setContent(countingInputStream);
			}

			IMetrics oMetrics = pExecutionContext.getMetrics();
			APIWebServiceResponse<? extends T> oAPIResponse;
			oMetrics.startEvent(APIRequestMetrics.ResponseProcessingTime);
			try {
				oAPIResponse = pResponseHandler.handle(pHttpResponse);
			} finally {
				oMetrics.endEvent(APIRequestMetrics.ResponseProcessingTime);
			}
			if (countingInputStream != null) {
				oMetrics.setCounter(APIRequestMetrics.BytesProcessed, countingInputStream.getByteCount());
			}

			if (oAPIResponse == null) throw new APIClientException("Unable to unmarshall response metadata");

			// TODO
			// responseMetadataCache.add(request.getOriginalRequest(), oAPIResponse.getResponseMetadata());

			if (mLogger.isDebugEnabled()) mLogger.debug("Received successful response: " + pApacheHttpResponse.getStatusLine().getStatusCode() + ", API Request ID: " + oAPIResponse.getRequestId());
			oMetrics.addProperty(APIRequestMetrics.APIRequestID, oAPIResponse.getRequestId());

			return oAPIResponse.getResult();
		} catch (CRC32MismatchException e) {
			throw e;
		} catch (Exception e) {
			String errorMessage = "Unable to unmarshall response (" + e.getMessage() + ")";
			throw new APIClientException(errorMessage, e);
		}
	}

	private HttpResponse createResponse(HttpRequestBase method, Request<?> request, org.apache.http.HttpResponse apacheHttpResponse) throws IOException {
		HttpResponse httpResponse = new HttpResponse(/* request, */method);

		if (apacheHttpResponse.getEntity() != null) {
			httpResponse.setContent(apacheHttpResponse.getEntity().getContent());
		}

		httpResponse.setStatusCode(apacheHttpResponse.getStatusLine().getStatusCode());
		httpResponse.setStatusText(apacheHttpResponse.getStatusLine().getReasonPhrase());
		for (Header header : apacheHttpResponse.getAllHeaders()) {
			httpResponse.addHeader(header.getName(), header.getValue());
		}

		return httpResponse;
	}

	private static boolean isTemporaryRedirect(org.apache.http.HttpResponse response) {
		int status = response.getStatusLine().getStatusCode();
		return status == HttpStatus.SC_TEMPORARY_REDIRECT &&
				response.getHeaders("Location") != null &&
				response.getHeaders("Location").length > 0;
	}

	private boolean isRequestSuccessful(org.apache.http.HttpResponse response) {
		int status = response.getStatusLine().getStatusCode();
		return status / 100 == HttpStatus.SC_OK / 100;
	}

	/**
	 * Sets a User-Agent for the specified request, taking into account
	 * any custom data.
	 */
	private void setUserAgent(Request<?> request) {
		String userAgent = getAPIConfiguration().getUserAgent();
		if (!userAgent.equals(APIConfiguration.DEFAULT_USER_AGENT)) {
			userAgent += ", " + APIConfiguration.DEFAULT_USER_AGENT;
		}
		if (userAgent != null) {
			request.addHeader(HEADER_USER_AGENT, userAgent);
		}
		// APIWebServiceRequest awsreq = request.getOriginalRequest();
		// TODO
		/*
		 * if (awsreq != null) {
		 * RequestClientOptions opts = awsreq.getRequestClientOptions();
		 * if (opts != null) {
		 * String userAgentMarker = opts.getClientMarker(Marker.USER_AGENT);
		 * if (userAgentMarker != null) {
		 * request.addHeader(HEADER_USER_AGENT,
		 * createUserAgentString(userAgent, userAgentMarker));
		 * }
		 * }
		 * }
		 */
	}
}
