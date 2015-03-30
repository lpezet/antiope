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
package com.github.lpezet.antiope2.metrics;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;

/**
 * @author luc
 *
 */
public class LogMetricsCollector implements IMetricsCollector {
	
	private boolean mEnabled;
	private Logger mLogger;
	
	public LogMetricsCollector() {
		mLogger = LoggerFactory.getLogger(LogMetricsCollector.class);
	}
	
	public LogMetricsCollector(String pLogName) {
		mLogger = LoggerFactory.getLogger(pLogName);
	}
	
	public LogMetricsCollector(Class<?> pLogClass) {
		mLogger = LoggerFactory.getLogger(pLogClass);
	}
	
	@Override
	//public <RQ extends APIWebServiceRequest, RS extends APIWebServiceResponse<?>> void collectMetrics(RQ pRequest, RS pResponse) {
	public <RQ extends IHttpRequest, RS extends IHttpResponse> void collectMetrics(RQ pRequest, RS pResponse) {
		latencyOfClientExecuteTime(pRequest, pResponse);
		latencyMetricOf(APIRequestMetrics.ResponseProcessingTime, pRequest, pResponse, false);
		latencyMetricOf(APIRequestMetrics.RequestSigningTime, pRequest, pResponse, false);
		latencyMetricOf(APIRequestMetrics.RequestMarshallTime, pRequest, pResponse, false);
		latencyMetricOf(APIRequestMetrics.HttpRequestTime, pRequest, pResponse, false);
	}
	
	/**
     * Returns all the latency metric data recorded for the specified metric
     * event type; or an empty list if there is none. The number of metric datum
     * in the returned list should be exactly one when there is no retries, or
     * more than one when there are retries.
     * 
     * @param includesRequestType
     *            true iff the "request" dimension is to be included;
     */
    //protected <RQ extends APIWebServiceRequest> void latencyMetricOf(MetricType metricType, RQ req, Object response, boolean includesRequestType) {
	protected <RQ extends IHttpRequest> void latencyMetricOf(MetricType metricType, RQ req, Object response, boolean includesRequestType) {
	    	IMetrics m = req.getExecutionContext().getMetrics();
    	//AWSRequestMetrics m = req.getAWSRequestMetrics();
        TimingInfo root = m.getTimingInfo();
        final String metricName = metricType.name();
        List<TimingInfo> subMeasures = root.getAllSubMeasurements(metricName);
        if (subMeasures != null) {
        	//List<MetricDatum> result = new ArrayList<MetricDatum>(subMeasures.size());
            for (TimingInfo sub : subMeasures) {
                if (sub.isEndTimeKnown()) { // being defensive
                	mLogger.trace("Service: {}, time: {}ms, metric: {}, requestType: {}", 
                			new Object[] { req.getServiceName(), sub.getTimeTakenMillisIfKnown(), metricName, requestType(req) });
                	/*
                    List<Dimension> dims = new ArrayList<Dimension>();
                    dims.add(new Dimension()
                            .withName(Dimensions.MetricType.name())
                            .withValue(metricName));
                    // Either a non request type specific datum is created per
                    // sub-measurement, or a request type specific one is 
                    // created but not both
                    if (includesRequestType) {
                        dims.add(new Dimension()
                                .withName(Dimensions.RequestType.name())
                                .withValue(requestType(req)));
                    }
                    MetricDatum datum = new MetricDatum()
                        .withMetricName(req.getServiceName())
                        .withDimensions(dims)
                        .withUnit(StandardUnit.Milliseconds)
                        .withValue(sub.getTimeTakenMillisIfKnown());
                    result.add(datum);
                    */
                }
            }
            //return result;
        }
        //return Collections.emptyList();
    }
	
	/**
     * Returns a request type specific metrics for
     * {@link APIRequestMetrics#ClientExecuteTime} which is special in the sense that it
     * makes a more accurate measurement by taking the {@link TimingInfo} at the
     * root into account.
     */
    //protected <RQ extends APIWebServiceRequest> void latencyOfClientExecuteTime(RQ req, Object response) {
	protected <RQ extends IHttpRequest> void latencyOfClientExecuteTime(RQ req, Object response) {
	    IMetrics m = req.getExecutionContext().getMetrics();
        TimingInfo root = m.getTimingInfo();
        final String metricName = APIRequestMetrics.ClientExecuteTime.name(); // Field.ClientExecuteTime.name();
        if (root.isEndTimeKnown()) { // being defensive
        	mLogger.trace("Service: {}, time: {}ms, metric: {}, requestType: {}", 
        			new Object[] { req.getServiceName(), root.getTimeTakenMillisIfKnown(), metricName, requestType(req) });
        	/*
            List<Dimension> dims = new ArrayList<Dimension>();
            dims.add(new Dimension()
                    .withName(Dimensions.MetricType.name())
                    .withValue(metricName));
            // request type specific
            dims.add(new Dimension()
                    .withName(Dimensions.RequestType.name())
                    .withValue(requestType(req)));
            MetricDatum datum = new MetricDatum()
                .withMetricName(req.getServiceName())
                .withDimensions(dims)
                .withUnit(StandardUnit.Milliseconds)
                .withValue(root.getTimeTakenMillisIfKnown());
            return Collections.singletonList(datum);
            */
        }
        //return Collections.emptyList();
    }
    
    /**
     * Returns the name of the type of request.
     */
    //private <RQ extends APIWebServiceRequest> String requestType(RQ req) {
	private <RQ extends IHttpRequest> String requestType(RQ req) {
	        return req.getClass().getSimpleName();// req.getOriginalRequest().getClass().getSimpleName();
    }
	
	public void setEnabled(boolean pEnabled) {
		mEnabled = pEnabled;
	}

	@Override
	public boolean isEnabled() {
		return mEnabled;
	}

}
