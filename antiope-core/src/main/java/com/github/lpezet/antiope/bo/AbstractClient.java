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
package com.github.lpezet.antiope.bo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.w3c.dom.Node;

import com.github.lpezet.antiope.APIServiceException;
import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.be.APIWebServiceRequest;
import com.github.lpezet.antiope.be.IAPICredentialsProvider;
import com.github.lpezet.antiope.dao.ExecutionContext;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.RequestHandler;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.dao.Signer;
import com.github.lpezet.antiope.metrics.APIRequestMetrics;
import com.github.lpezet.antiope.metrics.BaseMetrics;
import com.github.lpezet.antiope.metrics.IMetrics;
import com.github.lpezet.antiope.metrics.IMetricsCollector;
import com.github.lpezet.antiope.metrics.StubMetricsCollector;
import com.github.lpezet.antiope.transform.Unmarshaller;

/**
 * @author luc
 *
 */
public abstract class AbstractClient {
	
	protected final List<Unmarshaller<APIServiceException, Node>> mExceptionUnmarshallers = new ArrayList<Unmarshaller<APIServiceException, Node>>();

	private IAPICredentialsProvider mCredentialsProvider;
	
	/** The service endpoint to which this client will send requests. */
    protected URI mEndpoint;
	
	/** Optional request handlers for additional request processing. */
    protected final List<RequestHandler> mRequestHandlers;

    /** The client configuration */
    protected APIConfiguration mAPIConfiguration;

    /** Optional offset (in seconds) to use when signing requests */
    protected int mTimeOffset;

    /** Signer for authenticating requests. */
    private Signer mSigner;
    
    /**
     * Client level metrics collector.
     */
    private IMetricsCollector mMetricsCollector;
    
	public AbstractClient(APIConfiguration pConfiguration) {
    	mAPIConfiguration = pConfiguration;
        //mClient = new TSGHttpClient(clientConfiguration, requestMetricCollector);
        mRequestHandlers = new CopyOnWriteArrayList<RequestHandler>();
    }

	public URI getEndpoint() {
		return mEndpoint;
	}
	public void setEndpoint(URI pEndpoint) {
		mEndpoint = pEndpoint;
	}
	public void setEndpoint(String pEndpoint) throws IllegalArgumentException {
		mEndpoint = ensureScheme(pEndpoint);
        //configSigner(uri);
    }

    /** Sets and returns the endpoint as a URI. */
    private URI ensureScheme(String endpoint) throws IllegalArgumentException {
        /*
         * If the endpoint doesn't explicitly specify a protocol to use, then
         * we'll defer to the default protocol specified in the client
         * configuration.
         */
        if (endpoint.contains("://") == false) {
            endpoint = (mAPIConfiguration.isSecure() ? "https" : "http") + "://" + endpoint;
        }

        try {
            return new URI(endpoint);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
	
	protected final ExecutionContext createExecutionContext(APIWebServiceRequest req) {
        //boolean isMetricsEnabled = isRequestMetricsEnabled(req) || isProfilingEnabled();
        ExecutionContext oResult = new ExecutionContext(mRequestHandlers, isProfilingEnabled() ? new BaseMetrics() : null);
        oResult.setSigner(getSigner());
        return oResult;
    }

    private boolean isProfilingEnabled() {
		return mAPIConfiguration.isProfilingEnabled();
	}

	protected final ExecutionContext createExecutionContext(Request<?> req) {
        return createExecutionContext(req.getOriginalRequest());
    }
    
   /**
     * Common routine to end a client API request/response execution and collect
     * the request metrics.  Caller of this routine is responsible for starting
     * the event for {@link APIRequestMetrics#ClientExecuteTime} and call this method
     * in a try-finally block. 
     * 
     * @param pMetrics Metrics
     * @param pRequest Request
     * @param pResponse Response
     */
    protected final void endClientExecution(IMetrics pMetrics, Request<?> pRequest, Response<?> pResponse) {
        if (pRequest != null) {
        	pMetrics.endEvent(APIRequestMetrics.ClientExecuteTime);
        	pMetrics.getTimingInfo().endTiming();
            IMetricsCollector c = findRequestMetricCollector(pRequest);
            c.collectMetrics(pRequest, pResponse);
        }
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
     * Returns the most specific request metric collector, starting from the
     * request level, then client level, then finally the API SDK level.
     * 
     * @param req
     * 			request.
     * @return IMetricsCollector
     */
    protected final IMetricsCollector findRequestMetricCollector(Request<?> req) {
        APIWebServiceRequest origReq = req.getOriginalRequest();
        IMetricsCollector mc = null;
        if (origReq != null) {
	        mc = origReq.getMetricsCollector();
	        if (mc != null) {
	            return mc;
	        }
	    }
        mc = getMetricsCollector();
        return mc == null ? StubMetricsCollector.getInstance() : mc;
    }
    
    /**
     * Returns true if request metric collection is applicable to the given
     * request; false otherwise.
     */
    private boolean isRequestMetricsEnabled(APIWebServiceRequest req) {
    	//return false;
    	IMetricsCollector c = req.getMetricsCollector(); // request level collector
        if (c != null && c.isEnabled()) {
            return true;
        }
        return false;//TODO: isRMCEnabledAtClientOrSdkLevel();
        
    }
	
    
    public APIConfiguration getAPIConfiguration() {
		return mAPIConfiguration;
	}
    
	public IAPICredentialsProvider getCredentialsProvider() {
		return mCredentialsProvider;
	}
	public void setCredentialsProvider(IAPICredentialsProvider pCredentialsProvider) {
		mCredentialsProvider = pCredentialsProvider;
	}

	public Signer getSigner() {
		return mSigner;
	}

	public void setSigner(Signer pSigner) {
		mSigner = pSigner;
	}
	
}
