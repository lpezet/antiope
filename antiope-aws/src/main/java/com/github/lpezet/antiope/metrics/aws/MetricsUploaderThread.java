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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;

/**
 * A internal daemon thread used to upload request metrics to Amazon CloudWatch.
 */
class MetricsUploaderThread extends Thread {
    private static final String THREAD_NAME = "java-sdk-metric-uploader";
    private volatile boolean mCancelled;
    private final AmazonCloudWatchClient mCloudwatchClient;
    private final Logger mLogger = LoggerFactory.getLogger(getClass());
    private final BlockingRequestBuilder mQIterator;

    MetricsUploaderThread(Config pConfig,
            BlockingQueue<MetricDatum> pQueue) {
        this(pConfig, 
             pQueue, 
             pConfig.getCloudWatchConfig().getCredentialsProvider() == null 
             ? new AmazonCloudWatchClient() 
             : new AmazonCloudWatchClient(pConfig.getCloudWatchConfig().getCredentialsProvider()));
    }

    MetricsUploaderThread(Config pConfig, 
        BlockingQueue<MetricDatum> pQueue,
        AmazonCloudWatchClient pClient) {
        super(THREAD_NAME);
        if (pConfig == null || pQueue == null) {
            throw new IllegalArgumentException();
        }
        mCloudwatchClient = pClient;
        mQIterator = new BlockingRequestBuilder(pConfig, pQueue);
        String oEndpoint = pConfig.getCloudWatchConfig().getCloudWatchEndPoint(); 
        if (oEndpoint != null) 
        	mCloudwatchClient.setEndpoint(oEndpoint);
        this.setPriority(MIN_PRIORITY);
        setDaemon(true);
    }

    @Override
    public void run() {
    	mLogger.info("MetricsUploaderThread running...");
        while (!mCancelled) {
            try {
                Iterable<PutMetricDataRequest> oRequests = mQIterator.nextUploadUnits();
                for (PutMetricDataRequest req: oRequests) {
                	if (mLogger.isDebugEnabled() && req != null) mLogger.debug("Cloudwatch metric data request: " + req.toString());
                    mCloudwatchClient.putMetricData(req);
                    Thread.yield();
                }
            } catch(InterruptedException e) {
                if (!mCancelled) {
                    mLogger.debug("Unexpected interruption ignored");
                }
            } catch(Throwable t) {
                mLogger.warn("Unexpected condition; soldier on", t);
                Thread.yield();
            }
        }
        mLogger.info("MetricsUploaderThread done.");
    }

    void cancel() { mCancelled = true; }

    public AmazonCloudWatchClient getCloudwatchClient() {
        return mCloudwatchClient;
    }
}
