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

import static com.github.lpezet.antiope.metrics.aws.CloudWatchConfig.NAMESPACE_DELIMITER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StatisticSet;
import com.amazonaws.util.AwsHostNameUtils;
import com.amazonaws.util.json.Jackson;
import com.github.lpezet.antiope.metrics.aws.spi.Dimensions;

/**
 * An internal builder used to retrieve the next batch of requests to be sent to
 * Amazon CloudWatch. Calling method {@link #nextUploadUnits()} blocks as
 * necessary.
 */
class BlockingRequestBuilder {
    private static final String OS_METRIC_NAME = MachineMetric.getOSMetricName();
    private final MachineMetricFactory mMachineMetricFactory;
    private final BlockingQueue<MetricDatum> mQueue;
    private final long mTimeoutNano;
    private MetricsConfig mMetricsConfig;

    BlockingRequestBuilder(Config pConfig, BlockingQueue<MetricDatum> pQueue) {
        mQueue = pQueue;
        mMachineMetricFactory = new MachineMetricFactory(pConfig);
        mMetricsConfig = pConfig.getMetricsConfig();
        mTimeoutNano = TimeUnit.MILLISECONDS.toNanos(pConfig.getCloudWatchConfig().getQueuePollTimeoutMilli());
    }

    /**
     * Returns the next batch of {@link PutMetricDataRequest} to be sent to
     * Amazon CloudWatch, blocking as necessary to gather and accumulate the
     * necessary statistics. If there is no metrics data, this call blocks
     * indefinitely. If there is metrics data, this call will block up to about
     * {@link CloudWatchMetricConfig#getQueuePollTimeoutMilli()} number of
     * milliseconds.
     */
    Iterable<PutMetricDataRequest> nextUploadUnits() throws InterruptedException {
        final Map<String,MetricDatum> oUniqueMetrics = new HashMap<String,MetricDatum>();
        long oStartNano = System.nanoTime();
        
        while(true) {
            final long oElapsedNano = System.nanoTime() - oStartNano;
            if (oElapsedNano >= mTimeoutNano) {
                return toPutMetricDataRequests(oUniqueMetrics);
            }
            MetricDatum oDatum = mQueue.poll(mTimeoutNano - oElapsedNano, TimeUnit.NANOSECONDS);
            if (oDatum == null) {
                // timed out
                if (oUniqueMetrics.size() > 0) {
                    // return whatever we have so far
                    return toPutMetricDataRequests(oUniqueMetrics);
                }
                // zero AWS related metrics
                if (mMetricsConfig.isMachineMetricExcluded()) {
                    // Short note: nothing to do, so just wait indefinitely.
                    // (Long note: There exists a pedagogical case where the
                    // next statement is executed followed by no subsequent AWS
                    // traffic whatsoever, and then the machine metric is enabled 
                    // via JMX.
                    // In such case, we require the metric generation to be
                    // disabled and then re-enabled (eg via JMX).
                    // So why not always wake up periodically instead of going
                    // into long wait ?
                    // I (hchar@) think we should optimize for the most typical
                    // cases instead of the edge cases. Going into long wait has
                    // the benefit of relatively less runtime footprint.)
                    oDatum = mQueue.take();   
                    oStartNano = System.nanoTime();
                }
            }
            // Note at this point datum is null if and only if there is no
            // pending AWS related metrics but machine metrics is enabled
            if (oDatum != null)
                summarize(oDatum, oUniqueMetrics);
        }
    }

    /**
     * Summarizes the given datum into the statistics of the respective unique metric.
     */
    private void summarize(MetricDatum pDatum, Map<String, MetricDatum> pUniqueMetrics) {
        Double pValue = pDatum.getValue();
        if (pValue == null) {
            return;
        }
        List<Dimension> oDims = pDatum.getDimensions();
        Collections.sort(oDims, DimensionComparator.INSTANCE);
        String oMetricName = pDatum.getMetricName();
        String k = oMetricName + Jackson.toJsonString(oDims);
        MetricDatum oStatDatum = pUniqueMetrics.get(k);
        if (oStatDatum == null) {
            oStatDatum = new MetricDatum()
                .withDimensions(pDatum.getDimensions())
                .withMetricName(oMetricName)
                .withUnit(pDatum.getUnit())
                .withStatisticValues(new StatisticSet()
                    .withMaximum(pValue)
                    .withMinimum(pValue)
                    .withSampleCount(0.0)
                    .withSum(0.0))
                ;
            pUniqueMetrics.put(k, oStatDatum);
        }
        StatisticSet oStat = oStatDatum.getStatisticValues();
        oStat.setSampleCount(oStat.getSampleCount() + 1.0);
        oStat.setSum(oStat.getSum() + pValue);
        if (pValue > oStat.getMaximum()) {
            oStat.setMaximum(pValue);
        } else if (pValue < oStat.getMinimum()) {
            oStat.setMinimum(pValue);
        }
    }
    /**
     * Consolidates the input metrics into a list of PutMetricDataRequest, each
     * within the maximum size limit imposed by CloudWatch.
     */
    private Iterable<PutMetricDataRequest> toPutMetricDataRequests(Map<String, MetricDatum> pUniqueMetrics) {
        // Opportunistically generates some machine metrics whenever there
        // is metrics consolidation
        for (MetricDatum oDatum: mMachineMetricFactory.generateMetrics()) {
            summarize(oDatum, pUniqueMetrics);
        }
        List<PutMetricDataRequest> oList = new ArrayList<PutMetricDataRequest>();
        List<MetricDatum> oData = new ArrayList<MetricDatum>();
        for (MetricDatum m: pUniqueMetrics.values()) {
            oData.add(m);
            if (oData.size() == CloudWatchConfig.MAX_METRICS_DATUM_SIZE) {
                oList.addAll(newPutMetricDataRequests(oData));
                oData.clear();
            }
        }

        if (oData.size() > 0) {
            oList.addAll(newPutMetricDataRequests(oData));
        }
        return oList;
    }

    private List<PutMetricDataRequest> newPutMetricDataRequests(Collection<MetricDatum> pData) {
        List<PutMetricDataRequest> oList = new ArrayList<PutMetricDataRequest>();
        final String ns = mMetricsConfig.getMetricNameSpace();
        PutMetricDataRequest oReq = newPutMetricDataRequest(pData, ns);
        oList.add(oReq);
        final boolean oPerHost = mMetricsConfig.isPerHostMetricEnabled();
        String oPerHostNameSpace = null;
        String oHostName = null;
        Dimension oHostDim = null;
        final boolean oSingleNamespace = mMetricsConfig.isSingleMetricNamespace();
        if (oPerHost) {
            oHostName = mMetricsConfig.getHostMetricName();
            oHostName = oHostName == null ? "" : oHostName.trim();
            if (oHostName.length() == 0)
                oHostName = AwsHostNameUtils.localHostName();
            oHostDim = dimension(Dimensions.Host, oHostName);
            if (oSingleNamespace) {
                oReq = newPutMetricDataRequest(pData, ns, oHostDim);
            } else {
                oPerHostNameSpace = ns + NAMESPACE_DELIMITER + oHostName;
                oReq = newPutMetricDataRequest(pData, oPerHostNameSpace);
            }
            oList.add(oReq);
        }
        String oJvmMetricName = mMetricsConfig.getJvmMetricName();
        if (oJvmMetricName != null) {
            oJvmMetricName = oJvmMetricName.trim();
            if (oJvmMetricName.length() > 0) {
                if (oSingleNamespace) {
                    Dimension oJvmDim = dimension(Dimensions.JVM, oJvmMetricName);
                    if (oPerHost) {
                        // If OS metrics are already included at the per host level,
                        // there is little reason, if any, to include them at the
                        // JVM level.  Hence the filtering.
                        oReq = newPutMetricDataRequest(
                                filterOSMetrics(pData), ns, oHostDim, oJvmDim);
                    } else {
                        oReq = newPutMetricDataRequest(pData, ns, oJvmDim);
                    }
                    
                } else {
                    String oPerJvmNameSpace = oPerHostNameSpace == null
                        ? ns + NAMESPACE_DELIMITER + oJvmMetricName
                        : oPerHostNameSpace + NAMESPACE_DELIMITER + oJvmMetricName
                        ;
                    // If OS metrics are already included at the per host level,
                    // there is little reason, if any, to include them at the
                    // JVM level.  Hence the filtering.
                    oReq = newPutMetricDataRequest
                        (oPerHost ? filterOSMetrics(pData) : pData, oPerJvmNameSpace);
                }
                oList.add(oReq);
            }
        }
        return oList;
    }

    /**
     * Return a collection of metrics almost the same as the input except with
     * all OS metrics removed.
     */
    private Collection<MetricDatum> filterOSMetrics(Collection<MetricDatum> pData) {
        Collection<MetricDatum> oOutput = new ArrayList<MetricDatum>(pData.size());
        for (MetricDatum datum: pData) {
            if (!OS_METRIC_NAME.equals(datum.getMetricName()))
                oOutput.add(datum);
        }
        return oOutput;
    }

    private PutMetricDataRequest newPutMetricDataRequest(
            Collection<MetricDatum> pData, final String pNamespace,
            final Dimension... pExtraDims) {
        if (pExtraDims != null) {
            // Need to add some extra dimensions.
            // To do so, we copy the metric data to avoid mutability problems.
            Collection<MetricDatum> oNewData = new ArrayList<MetricDatum>(pData.size());
            for (MetricDatum md: pData) {
                MetricDatum oNewMD = cloneMetricDatum(md);
                for (Dimension dim: pExtraDims)
                    oNewMD.withDimensions(dim);  // add the extra dimensions to the new metric datum
                oNewData.add(oNewMD);
            }
            pData = oNewData;
        }
        return new PutMetricDataRequest()
            .withNamespace(pNamespace)
            .withMetricData(pData)
            .withRequestMetricCollector(RequestMetricCollector.NONE)
            ;
    }

    /**
     * Returns a metric datum cloned from the given one.
     * Made package private only for testing purposes.
     */
    final MetricDatum cloneMetricDatum(MetricDatum pMd) {
        return new MetricDatum()
            .withDimensions(pMd.getDimensions()) // a new collection is created
            .withMetricName(pMd.getMetricName())
            .withStatisticValues(pMd.getStatisticValues())
            .withTimestamp(pMd.getTimestamp())
            .withUnit(pMd.getUnit())
            .withValue(pMd.getValue());
    }

    private Dimension dimension(Dimensions pName, String pValue) {
        return new Dimension().withName(pName.toString()).withValue(pValue);
    }
}
