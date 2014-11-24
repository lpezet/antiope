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
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.annotation.ThreadSafe;

import com.amazonaws.metrics.AwsSdkMetrics;
import com.amazonaws.metrics.ByteThroughputProvider;
import com.amazonaws.metrics.MetricType;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.metrics.ServiceLatencyProvider;
import com.amazonaws.metrics.ServiceMetricCollector;
import com.amazonaws.metrics.ServiceMetricType;
import com.amazonaws.metrics.ThroughputMetricType;
import com.github.lpezet.antiope.metrics.aws.spi.Dimensions;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

/**
 * This is the default implementation of an AWS SDK service metric collection
 * system.
 * 
 * @see RequestMetricCollector
 */
@ThreadSafe
public class ServiceMetricsCollectorSupport extends ServiceMetricCollector {
    static final double NANO_PER_SEC = TimeUnit.SECONDS.toNanos(1);
    protected final static Log log = LogFactory.getLog(ServiceMetricsCollectorSupport.class);
    private final BlockingQueue<MetricDatum> mQueue;

    protected ServiceMetricsCollectorSupport(Config pConfig, BlockingQueue<MetricDatum> pQueue) {
        this.mQueue = pQueue;
    }

    @Override
    public void collectByteThroughput(ByteThroughputProvider pProvider) {
        try {
            collectByteThroughput0(pProvider);
        } catch(Exception ex) { // defensive code
            if (log.isDebugEnabled()) {
                log.debug("Ignoring unexpected failure", ex);
            }
        }
    }
    
    /** 
     * Returns the number of bytes per second, given the byte count and
     * duration in nano seconds.  Duration of zero nanosecond will be treated
     * as 1 nanosecond.
     */
    double bytesPerSecond(double pByteCount, double pDurationNano) {
        if (pByteCount < 0 || pDurationNano < 0)
            throw new IllegalArgumentException();
        if (pDurationNano == 0) {
            pDurationNano = 1.0;   // defend against division by zero
            if (log.isDebugEnabled()) {
                log.debug("Set zero to one to avoid division by zero; but should never get here!");
            }
        }
        double oBytesPerSec = (pByteCount / pDurationNano) * NANO_PER_SEC;
        if (oBytesPerSec == 0) {
            if (log.isDebugEnabled()) {
                log.debug("zero bytes per sec.  Really ?");
            }
        }
        return oBytesPerSec;
    }
    
    private void collectByteThroughput0(ByteThroughputProvider pProvider) {
        final ThroughputMetricType oThroughputType = pProvider.getThroughputMetricType();
        final ServiceMetricType oByteCountType = oThroughputType.getByteCountMetricType();
        final Set<MetricType> oMetrics = AwsSdkMetrics.getPredefinedMetrics();
        final double oByteCount = pProvider.getByteCount();
        double oDurationNano = pProvider.getDurationNano();
        double oBytesPerSec = bytesPerSecond(oByteCount, oDurationNano);
        if (oMetrics.contains(oThroughputType)) {
            // Throughput metric
            final Dimension oThroughputDimension = new Dimension()
                .withName(Dimensions.MetricType.name())
                .withValue(oThroughputType.name());
            final MetricDatum oThroughputDatum = new MetricDatum()
                .withMetricName(oThroughputType.getServiceName())
                .withDimensions(oThroughputDimension)
                .withUnit(StandardUnit.BytesSecond)
                .withValue(oBytesPerSec);
            safeAddMetricsToQueue(oThroughputDatum);
        }
        if (oMetrics.contains(oByteCountType)) {
            // Byte count metric
            final Dimension oByteCountDimension = new Dimension()
                .withName(Dimensions.MetricType.name())
                .withValue(oByteCountType.name());
            final MetricDatum oByteCountDatum = new MetricDatum()
                .withMetricName(oByteCountType.getServiceName())
                .withDimensions(oByteCountDimension)
                .withUnit(StandardUnit.Bytes)
                .withValue(oByteCount);
            safeAddMetricsToQueue(oByteCountDatum);
        }
    }

    @Override
    public void collectLatency(ServiceLatencyProvider pProvider) {
        final ServiceMetricType oType = pProvider.getServiceMetricType();
        final Set<MetricType> oMetrics = AwsSdkMetrics.getPredefinedMetrics();
        if (oMetrics.contains(oType)) {
            final Dimension oDim = new Dimension()
                .withName(Dimensions.MetricType.name())
                .withValue(oType.name());
            final MetricDatum oDatum = new MetricDatum()
                .withMetricName(oType.getServiceName())
                .withDimensions(oDim)
                .withUnit(StandardUnit.Milliseconds)
                .withValue(pProvider.getDurationMilli());
            safeAddMetricsToQueue(oDatum);
        }
    }

    private void safeAddMetricsToQueue(MetricDatum pMetric) {
        try {
            if (!addMetricsToQueue(pMetric)) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to add to the metrics queue (due to no space available) for "
                            + pMetric.getMetricName());
                }
            }
        } catch (RuntimeException ex) {
            log.warn("Failed to add to the metrics queue for metric: " + pMetric,
                    ex);
        }
    }
    /**
     * Adds the given metric to the queue, returning true if successful or false
     * if no space available.
     */
    protected boolean addMetricsToQueue(MetricDatum pMetric) {
        return mQueue.offer(pMetric); 
    }
}