/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.github.lpezet.antiope.metrics.aws;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.metrics.APIRequestMetrics;
import com.github.lpezet.antiope.metrics.IMetrics;
import com.github.lpezet.antiope.metrics.IMetricsCollector;
import com.github.lpezet.antiope.metrics.MetricType;
import com.github.lpezet.antiope.metrics.aws.spi.PredefinedMetricTransformer;

/**
 * This is the default implementation of an AWS SDK request metric collection
 * system.
 * 
 * @see RequestMetricsCollector
 */
@ThreadSafe
public class RequestMetricsCollectorSupport implements IMetricsCollector {
    protected final static Logger mLogger = LoggerFactory.getLogger(RequestMetricsCollectorSupport.class);
    private final BlockingQueue<MetricDatum> mQueue;
    private final PredefinedMetricTransformer mTransformer = new PredefinedMetricTransformer();
    private final Set<MetricType> mPredefinedMetrics;
    
    /*
    static {
    	PREDIFIED_METRICS.add(APIRequestMetrics.ClientExecuteTime);
    	PREDIFIED_METRICS.add(APIRequestMetrics.Exception);
    	PREDIFIED_METRICS.add(APIRequestMetrics.ThrottleException);
    	PREDIFIED_METRICS.add(APIRequestMetrics.HttpClientRetryCount);
    	PREDIFIED_METRICS.add(APIRequestMetrics.HttpRequestTime);
    	PREDIFIED_METRICS.add(APIRequestMetrics.RequestCount);
    	PREDIFIED_METRICS.add(APIRequestMetrics.RetryCount);
    	PREDIFIED_METRICS.add(APIRequestMetrics.HttpClientSendRequestTime);
    	PREDIFIED_METRICS.add(APIRequestMetrics.HttpClientReceiveResponseTime);
    	PREDIFIED_METRICS.add(APIRequestMetrics.HttpClientPoolAvailableCount);
    	PREDIFIED_METRICS.add(APIRequestMetrics.HttpClientPoolLeasedCount);
    	PREDIFIED_METRICS.add(APIRequestMetrics.HttpClientPoolPendingCount);
    	//TODO: AWSServiceMetrics.HttpClientGetConnectionTime
    }
    */
    
    protected RequestMetricsCollectorSupport(Config pConfig, BlockingQueue<MetricDatum> pQueue) {
        this.mQueue = pQueue;
        mPredefinedMetrics = pConfig.getMetricsConfig().getPredefinedMetrics();
    }

    /**
     * Collects the metrics at the end of a request/response cycle, transforms
     * the metric data points into a cloud watch metric datum representation,
     * and then adds it to a memory queue so it will get summarized into the
     * necessary statistics and uploaded to Amazon CloudWatch.
     */
    @Override
    public void collectMetrics(Request<?> pRequest, Response<?> pResponse) {
        try {
            collectMetrics0(pRequest, pResponse);
        } catch(Exception ex) { // defensive code
            if (mLogger.isDebugEnabled()) {
                mLogger.debug("Ignoring unexpected failure", ex);
            }
        }
    }
    
    @Override
    public boolean isEnabled() {
    	return true;
    }
    
    private void collectMetrics0(Request<?> pRequest, Response<?> pResponse) {
        IMetrics oArm = pRequest.getMetrics();
        if (oArm == null) { // || !arm.isEnabled()) {
            return;
        }
        for (MetricType oType: mPredefinedMetrics) {
            if (!(oType instanceof APIRequestMetrics))
                continue;
            //if (mLogger.isDebugEnabled()) mLogger.debug("Collecting metric: " + oType);
            PredefinedMetricTransformer oTransformer = getTransformer();
            for (MetricDatum datum : oTransformer.toMetricData(oType, pRequest, pResponse)) {
                try {
                    if (!addMetricsToQueue(datum)) {
                        if (mLogger.isDebugEnabled()) {
                            mLogger.debug("Failed to add to the metrics queue (due to no space available) for "
                                    + oType.name()
                                    + ":"
                                    + pRequest.getServiceName());
                        }
                    }
                } catch(RuntimeException ex) {
                    mLogger.warn("Failed to add to the metrics queue for "
                        + oType.name() + ":" + pRequest.getServiceName(),
                        ex);
                }
            }
        }
    }

    /**
     * Adds the given metric to the queue, returning true if successful or false
     * if no space available.
     */
    protected boolean addMetricsToQueue(MetricDatum pMetric) {
        return mQueue.offer(pMetric); 
    }
    /** Returns the predefined metrics transformer. */
    protected PredefinedMetricTransformer getTransformer() { return mTransformer; }
}