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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;

/**
 * TODO: Change this implementation so it's no longer a singleton. 
 * Or at least something that can be used across API clients.
 * 
 * This is the default implementation of an AWS SDK request metric collection
 * system.
 * 
 * @see RequestMetricCollector
 */
@ThreadSafe
public class MetricsCollectorSupport extends ThreadedMetricsCollector {
    protected final static Logger mLogger = LoggerFactory.getLogger(MetricsCollectorSupport.class);
    private final MetricsQueueCollection mMetricsCollectorImpl;

    private final BlockingQueue<MetricDatum> mQueue;
    private final Config mConfig;
    private MetricsUploaderThread mUploaderThread;

    public MetricsCollectorSupport(Config pConfig) {
        if (pConfig == null) {
            throw new IllegalArgumentException();
        }
        mConfig = pConfig;
        mQueue = new LinkedBlockingQueue<MetricDatum>(pConfig.getCloudWatchConfig().getMetricQueueSize());
        mMetricsCollectorImpl = new MetricsQueueCollection(pConfig, mQueue);
        //mRequestMetricsCollector = new RequestMetricsCollectorSupport(pConfig, mQueue);
        //mServiceMetricsCollector = new ServiceMetricsCollectorSupport(pConfig, mQueue);
    }

    @Override
    public boolean start() {
        synchronized(MetricsCollectorSupport.class) {
            if (mUploaderThread != null) {
                return false;   // already started
            }
            mUploaderThread = new MetricsUploaderThread(mConfig, mQueue);
            mUploaderThread.start();
        }
        return true;
    }

    /**
     * Stops this collector immediately, dropping all pending metrics in memory.
     */
    @Override
    public boolean stop() {
        synchronized(MetricsCollectorSupport.class) {
            if (mUploaderThread != null) {
                mUploaderThread.cancel();
                mUploaderThread.interrupt();
                mUploaderThread = null;
                return true;
            }
        }
        return false;
    }

    /** Returns the configuration. */
    public Config getConfig() { return mConfig; }

    public AmazonCloudWatchClient getCloudwatchClient() {
        return mUploaderThread == null ? null : mUploaderThread.getCloudwatchClient();
    }
    /** Always returns true. */
    @Override public final boolean isEnabled() { return true; }
    
    @Override
    public void collectMetrics(Request<?> pRequest, Response<?> pResponse) {
    	mMetricsCollectorImpl.collectMetrics(pRequest, pResponse);
    }
}