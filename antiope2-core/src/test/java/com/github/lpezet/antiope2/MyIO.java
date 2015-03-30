/**
 * 
 */
package com.github.lpezet.antiope2;

import com.github.lpezet.antiope2.dao.IMarshaller;
import com.github.lpezet.antiope2.dao.INetworkIO;
import com.github.lpezet.antiope2.dao.IUnmarshaller;
import com.github.lpezet.antiope2.dao.http.HttpNetworkIOs;
import com.github.lpezet.antiope2.dao.http.IHttpNetworkIO;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;
import com.github.lpezet.antiope2.metrics.APIRequestMetrics;
import com.github.lpezet.antiope2.metrics.IMetrics;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;
import com.github.lpezet.antiope2.metrics.StubMetricsCollector;

/**
 * @author Luc Pezet
 */
public class MyIO<RQ, RS> implements INetworkIO<RQ, RS> {

	private IHttpNetworkIO<IHttpRequest, IHttpResponse>	mNetworkIO;
	private IMarshaller<RQ, IHttpRequest>				mMarshaller;
	private IUnmarshaller<IHttpResponse, RS>			mUnmarshaller;
	private IMetricsCollector							mMetricsCollector;

	public MyIO(IMarshaller<RQ, IHttpRequest> pMarshaller, IHttpNetworkIO<IHttpRequest, IHttpResponse> pNetworkIO, IUnmarshaller<IHttpResponse, RS> pUnmarshaller) {
		this(pMarshaller, pNetworkIO, pUnmarshaller, null);
	}
	/**
	 * @param pMarshaller
	 * @param pNetworkIO
	 *            Raw HttpNetworkIO, i.e. not extending AbstractHttpNetworkIO.
	 * @param pUnmarshaller
	 */
	public MyIO(IMarshaller<RQ, IHttpRequest> pMarshaller, IHttpNetworkIO<IHttpRequest, IHttpResponse> pNetworkIO, IUnmarshaller<IHttpResponse, RS> pUnmarshaller, IMetricsCollector pMetricsCollector) {
		mMarshaller = pMarshaller;
		mNetworkIO = HttpNetworkIOs.nonClosingIO(pNetworkIO);
		mUnmarshaller = pUnmarshaller;
		mMetricsCollector = pMetricsCollector;
	}

	/**
	 * Common routine to end a client API request/response execution and collect
	 * the request metrics. Caller of this routine is responsible for starting
	 * the event for {@link APIRequestMetrics#ClientExecuteTime} and call this method
	 * in a try-finally block.
	 */
	protected final void endClientExecution(IMetrics pMetrics, IHttpRequest pRequest, IHttpResponse pResponse) {
		if (pRequest != null) {
			pMetrics.endEvent(APIRequestMetrics.ClientExecuteTime);
			pMetrics.getTimingInfo().endTiming();
			IMetricsCollector c = findRequestMetricCollector(pRequest);
			c.collectMetrics(pRequest, pResponse);
		}
	}

	/**
	 * Returns the most specific request metric collector, starting from the
	 * request level, then client level, then finally the API SDK level.
	 * 
	 * @param req
	 *            request.
	 * @return IMetricsCollector
	 */
	protected final IMetricsCollector findRequestMetricCollector(IHttpRequest req) {
		IMetricsCollector mc = req.getExecutionContext().getMetricsCollector();
		if (mc != null) {
			return mc;
		}
		mc = getMetricsCollector();
		return mc == null ? StubMetricsCollector.getInstance() : mc;
	}

	@Override
	public RS perform(RQ pRequest) throws Exception {
		IHttpRequest oHttpRequest = mMarshaller.perform(pRequest);
		IHttpResponse oHttpResponse = null;
		
		IMetrics oMetrics = oHttpRequest.getExecutionContext().getMetrics();
		oMetrics.startEvent(APIRequestMetrics.ClientExecuteTime);
    	
		try {
			oHttpResponse = mNetworkIO.perform(oHttpRequest);
			RS oResponse = mUnmarshaller.perform(oHttpResponse);
			return oResponse;
		} finally {
			if (oHttpResponse != null) oHttpResponse.close();
			endClientExecution(oMetrics, oHttpRequest, oHttpResponse);
		}
		
	}

	public IMetricsCollector getMetricsCollector() {
		return mMetricsCollector;
	}

}
