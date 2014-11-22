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
 * This is the default implementation of an AWS SDK request metric collection
 * system.
 * 
 * @see RequestMetricCollector
 */
@ThreadSafe
public class MetricsCollectorSupport extends ThreadedMetricsCollector {
    protected final static Logger mLogger = LoggerFactory.getLogger(MetricsCollectorSupport.class);
    private static volatile MetricsCollectorSupport singleton;
    
    /** Returns the singleton instance; or null if there isn't one. */
    static MetricsCollectorSupport getInstance() {
        return singleton;
    }

    /** 
     * Starts a new CloudWatch collector if it's not already started.
     *
     * @return true if it is successfully started by this call; false if the
     * collector is already running or if there is failure in trying to start
     * the collector for the first time. 
     */
    static synchronized boolean startSingleton(Config pConfig) {
        if (singleton != null) {
            return false;
        }
        mLogger.info("Initializing " + MetricsCollectorSupport.class.getSimpleName());
        return createAndStartCollector(pConfig);
    }

    /** Retarts with a new CloudWatch collector. */
    static synchronized boolean restartSingleton(Config pConfig) {
        if (singleton == null) {
            throw new IllegalStateException(MetricsCollectorSupport.class.getSimpleName()
                + " has neven been initialized");
        }
        mLogger.info("Re-initializing " + MetricsCollectorSupport.class.getSimpleName());
        singleton.stop();
        // singleton is set to null at this point via the stop method
        return createAndStartCollector(pConfig);
    }

    /**
     * Returns true if the collector is successfully created and started;
     * false otherwise.
     */
    private static boolean createAndStartCollector(Config pConfig) {
    	MetricsCollectorSupport oCollector = new MetricsCollectorSupport(pConfig);
        if (oCollector.start()) {
            singleton = oCollector;
            return true;
        }
        return false;
    }

    private final RequestMetricsCollectorSupport mRequestMetricsCollector;
    private final ServiceMetricsCollectorSupport mServiceMetricsCollector;

    private final BlockingQueue<MetricDatum> mQueue;
//    private final PredefinedMetricTransformer transformer = new PredefinedMetricTransformer();
    private final Config mConfig;
    private MetricsUploaderThread mUploaderThread;

    protected MetricsCollectorSupport(Config pConfig) {
        if (pConfig == null) {
            throw new IllegalArgumentException();
        }
        mConfig = pConfig;
        mQueue = new LinkedBlockingQueue<MetricDatum>(pConfig.getCloudWatchConfig().getMetricQueueSize());
        mRequestMetricsCollector = new RequestMetricsCollectorSupport(mQueue);
        mServiceMetricsCollector = new ServiceMetricsCollectorSupport(mQueue);
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
                if (singleton == this) { // defensive check
                    singleton = null;
                }
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
    	mRequestMetricsCollector.collectMetrics(pRequest, pResponse);
    }
}