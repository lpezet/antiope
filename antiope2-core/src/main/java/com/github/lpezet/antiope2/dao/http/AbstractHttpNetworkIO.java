/**
 * 
 */
package com.github.lpezet.antiope2.dao.http;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lpezet.antiope2.APIClientException;
import com.github.lpezet.antiope2.dao.ExecutionContext;
import com.github.lpezet.antiope2.metrics.APIRequestMetrics;
import com.github.lpezet.antiope2.metrics.IMetrics;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;

/**
 * @author Luc Pezet
 *
 */
public abstract class AbstractHttpNetworkIO<RQ extends IHttpRequest, RS extends IHttpResponse> implements IHttpNetworkIO<IHttpRequest, IHttpResponse> {

	private Logger mLogger = LoggerFactory.getLogger(this.getClass());
	private IMetricsCollector mMetricsCollector;
	
	@Override
	public IHttpResponse perform(IHttpRequest pRequest) throws Exception {
		ExecutionContext oEC = pRequest.getExecutionContext();
		IMetrics oMetrics = oEC.getMetrics();
		// Add service metrics.
		oMetrics.addProperty(APIRequestMetrics.ServiceName, pRequest.getServiceName());
		oMetrics.addProperty(APIRequestMetrics.ServiceEndpoint, pRequest.getEndpoint());

		IHttpResponse oResponse = null;
		try {
			if (mLogger.isDebugEnabled()) mLogger.debug("Sending Request: " + pRequest.toString());
			
			oMetrics.startEvent(APIRequestMetrics.HttpRequestTime);
			try {
				oResponse = doPerform(pRequest);
			} finally {
				oMetrics.endEvent(APIRequestMetrics.HttpRequestTime);
			}
			oMetrics.addProperty(APIRequestMetrics.StatusCode, oResponse.getStatusCode());

		} catch (IOException ioe) {
			if (mLogger.isInfoEnabled()) mLogger.info("Unable to execute HTTP request: " + ioe.getMessage(), ioe);
			oMetrics.incrementCounter(APIRequestMetrics.Exception);
			oMetrics.addProperty(APIRequestMetrics.Exception, ioe.toString());
			oMetrics.addProperty(APIRequestMetrics.APIRequestID, null);

			APIClientException ace = new APIClientException("Unable to execute HTTP request: " + ioe.getMessage(), ioe);
			// TODO
			/*
			 * if (!shouldRetry(request.getOriginalRequest(),
			 * httpRequest,
			 * ace,
			 * requestCount,
			 * config.getRetryPolicy())) {
			 * throw ace;
			 * }
			 */
			// Cache the retryable exception
			// oRetriedException = ace;
			// resetRequestAfterError(pRequest, ioe);
			throw ace;
		} catch (RuntimeException e) {
			throw handleUnexpectedFailure(e, oMetrics);
		} catch (Error e) {
			throw handleUnexpectedFailure(e, oMetrics);
		} finally {
			postPerform(pRequest, oResponse);
			/*
			 * Some response handlers need to manually manage the HTTP
			 * connection and will take care of releasing the connection on
			 * their own, but if this response handler doesn't need the
			 * connection left open, we go ahead and release the it to free
			 * up resources.
			 */
			/*
			 if (oResponseHandler != null && !oResponseHandler.needsConnectionLeftOpen()) {
				 try {
					 if (oApacheResponse != null) {
						 if (oApacheResponse.getEntity() != null
							 && oApacheResponse.getEntity().getContent() != null) {
						 oApacheResponse.getEntity().getContent().close();
						 }
						 
						 if (oApacheResponse instanceof CloseableHttpResponse) {
							 CloseableHttpResponse oCloseable = (CloseableHttpResponse) oApacheResponse;
							 oCloseable.close();
						 }
					 }
				 } catch (IOException e) {
				 mLogger.warn("Cannot close the response content.", e);
				 }
			 }
			 */
		}
		return oResponse;
	}
	
	/**
     * Returns the client specific {@link IMetricsCollector}; or null if
     * there is none.
     * 
     * @return IMetricsCollector
     */
    public IMetricsCollector getMetricsCollector() {
		return mMetricsCollector;
	}
    
    public void setMetricsCollector(IMetricsCollector pMetricsCollector) {
		mMetricsCollector = pMetricsCollector;
	}

	/**
	 * Handles an unexpected failure, returning the Throwable instance as given.
	 */
	protected <T extends Throwable> T handleUnexpectedFailure(T t, IMetrics pMetrics) {
		pMetrics.incrementCounter(APIRequestMetrics.Exception);
		return t;
	}

	protected abstract IHttpResponse doPerform(IHttpRequest pRequest) throws Exception;
	
	protected abstract void postPerform(IHttpRequest pRequest, IHttpResponse pResponse);
}
