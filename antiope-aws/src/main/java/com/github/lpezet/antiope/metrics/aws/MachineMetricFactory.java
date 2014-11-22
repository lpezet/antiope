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

import static com.github.lpezet.antiope.metrics.aws.MachineMetric.DaemonThreadCount;
import static com.github.lpezet.antiope.metrics.aws.MachineMetric.DeadLockThreadCount;
import static com.github.lpezet.antiope.metrics.aws.MachineMetric.FreeMemory;
import static com.github.lpezet.antiope.metrics.aws.MachineMetric.OpenFileDescriptorCount;
import static com.github.lpezet.antiope.metrics.aws.MachineMetric.PeakThreadCount;
import static com.github.lpezet.antiope.metrics.aws.MachineMetric.SpareFileDescriptorCount;
import static com.github.lpezet.antiope.metrics.aws.MachineMetric.SpareMemory;
import static com.github.lpezet.antiope.metrics.aws.MachineMetric.ThreadCount;
import static com.github.lpezet.antiope.metrics.aws.MachineMetric.TotalMemory;
import static com.github.lpezet.antiope.metrics.aws.MachineMetric.TotalStartedThreadCount;
import static com.github.lpezet.antiope.metrics.aws.MachineMetric.UsedMemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import com.amazonaws.jmx.spi.JmxInfoProvider;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.github.lpezet.antiope.metrics.MetricType;

class MachineMetricFactory {
    private static final List<MachineMetric> MEMORY_METRICS = Arrays.asList(
            TotalMemory, FreeMemory, UsedMemory, SpareMemory);
    private static final List<MachineMetric> THREAD_METRICS = Arrays.asList(
            ThreadCount, DeadLockThreadCount, DaemonThreadCount,
            PeakThreadCount, TotalStartedThreadCount);
    private static final List<MachineMetric> FD_METRICS = Arrays.asList(
            OpenFileDescriptorCount, SpareFileDescriptorCount);
    private final JmxInfoProvider mJmxInfoProvider = JmxInfoProvider.Factory.getJmxInfoProvider();

    private Config mConfig;
    
    public MachineMetricFactory(Config pConfig) {
    	mConfig = pConfig;
	}
    
    /**
     * Add the given list of metrics and corresponding values specified in
     * "metricValues" to the given list of metric datum.
     * 
     * @param pList
     *            list of metric data
     * @param pMetricValues
     *            list of metrics and their corresponding values
     */
    private void addMetrics(List<MetricDatum> pList, MetricValues pMetricValues, StandardUnit pUnit) {
        List<MachineMetric> oMachineMetrics = pMetricValues.getMetrics();
        List<Long> oValues = pMetricValues.getValues();
        for (int i=0; i < oMachineMetrics.size(); i++) {
            MachineMetric oMetric = oMachineMetrics.get(i);
            long val = oValues.get(i).longValue();
            // skip zero values in some cases
            if (val != 0 || oMetric.includeZeroValue()) {
                MetricDatum oDatum = new MetricDatum()
                    .withMetricName(oMetric.getMetricName())
                    .withDimensions(
                        new Dimension()
                        .withName(oMetric.getDimensionName())
                        .withValue(oMetric.name()))
                    .withUnit(pUnit)
                    .withValue((double) val)
                    ;
                pList.add(oDatum);
            }
        }
    }

    /**
     * Returns the set of custom machine metrics specified in the SDK metrics
     * registry; or an empty set if there is none. Note any machine metrics
     * found in the registry must have been custom specified, as the default
     * behavior is to include all machine metrics when enabled.
     * 
     * @return a non-null set of machine metrics. An empty set means no custom
     *         machine metrics have been specified.
     */
    private Set<MachineMetric> customMachineMetrics() {
        Set<MachineMetric> oCustomized = new HashSet<MachineMetric>();
        for (MetricType m: mConfig.getMetricsConfig().getPredefinedMetrics()) {
            if (m instanceof MachineMetric)
                oCustomized.add((MachineMetric)m);
        }
        return oCustomized;
    }

    /**
     * Returns a subset of the given list of metrics in "defaults" and the
     * corresponding value of each returned metric in the subset. Note if the
     * custom set is empty, the full set of default machine metrics and values
     * will be returned. (In particular, as in set theory, a set is a subset of
     * itself.)
     * 
     * @param pCustomSet
     *            custom machine metrics specified in the SDK metrics registry
     * @param pDefaults
     *            the given default list of metrics
     * @param pValues
     *            corresponding values of each metric in "defaults"
     */
    private MetricValues metricValues(Set<MachineMetric> pCustomSet, List<MachineMetric> pDefaults, List<Long> pValues) {
        List<MachineMetric> oActualMetrics = pDefaults;
        List<Long> oActualValues = pValues;
        if (pCustomSet.size() > 0) {
            // custom set of machine metrics specified
            oActualMetrics = new ArrayList<MachineMetric>();
            oActualValues = new ArrayList<Long>();
            for (int i=0; i < pDefaults.size(); i++) {
                MachineMetric mm = pDefaults.get(i);
                if (pCustomSet.contains(mm)) {
                    oActualMetrics.add(mm);
                    oActualValues.add(pValues.get(i));
                }
            }
        }
        return new MetricValues(oActualMetrics, oActualValues);
    }

    List<MetricDatum> generateMetrics() {
        if (mConfig.getMetricsConfig().isMachineMetricExcluded())
            return Collections.emptyList();
        Set<MachineMetric> oCustomSet = customMachineMetrics();
        List<MetricDatum> oTargetList = new ArrayList<MetricDatum>(
                MachineMetric.values().length);
        // Memory usage
        addMemoryMetrics(oTargetList, oCustomSet);
        // Thread related counts
        try {
            addThreadMetrics(oTargetList, oCustomSet);
        } catch (Throwable t) {
            LogFactory.getLog(getClass()).debug("Ignoring thread metrics", t);
        }
        // File descriptor usage
        try {
            addFileDescriptorMetrics(oTargetList, oCustomSet);
        } catch (Throwable t) {
            LogFactory.getLog(getClass()).debug("Ignoring file descriptor metrics", t);
        }
        return oTargetList;
    }

    private void addMemoryMetrics(List<MetricDatum> pTargetList, Set<MachineMetric> pCustomSet) {
        Runtime rt = Runtime.getRuntime();
        long oTotalMem = rt.totalMemory();
        long oFreeMem = rt.freeMemory();
        long oUsedMem = oTotalMem - oFreeMem;
        long oSpareMem = rt.maxMemory() - oUsedMem;
        List<Long> oValues = Arrays.asList(oTotalMem, oFreeMem, oUsedMem, oSpareMem);
        MetricValues oMetricValues = memoryMetricValues(pCustomSet, oValues);
        addMetrics(pTargetList, oMetricValues, StandardUnit.Bytes);
    }

    private void addFileDescriptorMetrics(List<MetricDatum> pTargetList, Set<MachineMetric> pCustomSet) {
        JmxInfoProvider oProvider = JmxInfoProvider.Factory.getJmxInfoProvider();
        long[] oFdInfo = oProvider.getFileDecriptorInfo();

        if (oFdInfo != null) {
            long oOpenFdCount = oFdInfo[0];
            long oMaxFdCount = oFdInfo[1];
            List<Long> oValues = Arrays.asList(oOpenFdCount, oMaxFdCount - oOpenFdCount);
            MetricValues oMetricValues = fdMetricValues(pCustomSet, oValues); 
            addMetrics(pTargetList, oMetricValues, StandardUnit.Count);
        }
    }

    private void addThreadMetrics(List<MetricDatum> pTargetList, Set<MachineMetric> pCustomSet) {
        long oThreadCount = mJmxInfoProvider.getThreadCount();
        long[] oIds = mJmxInfoProvider.findDeadlockedThreads();
        long oDeadLockThreadCount = oIds == null ? 0 : oIds.length;
        long oDaemonThreadCount = mJmxInfoProvider.getDaemonThreadCount();
        long oPeakThreadCount = mJmxInfoProvider.getPeakThreadCount();
        long oTotalStartedThreadCount = mJmxInfoProvider.getTotalStartedThreadCount();
        List<Long> oValues = Arrays.asList(oThreadCount,
            oDeadLockThreadCount,
            oDaemonThreadCount,
            oPeakThreadCount,
            oTotalStartedThreadCount);
        MetricValues oMetricValues = threadMetricValues(pCustomSet, oValues); 
        addMetrics(pTargetList, oMetricValues, StandardUnit.Count);
    }

    /**
     * Returns the set of memory metrics and the corresponding values based on
     * the default and the customized set of metrics, if any.
     * 
     * @param pCustomSet
     *            a non-null customized set of metrics
     * @param pValues
     *            a non-null list of values corresponding to the list of default
     *            memory metrics
     */
    private MetricValues memoryMetricValues(Set<MachineMetric> pCustomSet,
            List<Long> pValues) {
        return metricValues(pCustomSet, MachineMetricFactory.MEMORY_METRICS,
                pValues);
    }

    /**
     * Returns the set of file-descriptor metrics and the corresponding values based on
     * the default and the customized set of metrics, if any.
     * 
     * @param pCustomSet
     *            a non-null customized set of metrics
     * @param pValues
     *            a non-null list of values corresponding to the list of default
     *            file-descriptor metrics
     */
    private MetricValues fdMetricValues(Set<MachineMetric> pCustomSet,
            List<Long> pValues) {
        return metricValues(pCustomSet, MachineMetricFactory.FD_METRICS, pValues);
    }

    /**
     * Returns the set of thread metrics and the corresponding values based on
     * the default and the customized set of metrics, if any.
     * 
     * @param pCustomSet
     *            a non-null customized set of metrics
     * @param pValues
     *            a non-null list of values corresponding to the list of default
     *            thread metrics
     */
    private MetricValues threadMetricValues(Set<MachineMetric> pCustomSet, List<Long> pValues) {
        return metricValues(pCustomSet, MachineMetricFactory.THREAD_METRICS,
                pValues);
    }

    // Used to get around the limitation of Java returning at most a single value
    private static class MetricValues {
        private final List<MachineMetric> mMetrics;
        private final List<Long> mValues;
        MetricValues(List<MachineMetric> pMetrics, List<Long> pValues) {
            this.mMetrics = pMetrics;
            this.mValues = pValues;
        }
        List<MachineMetric> getMetrics() { return mMetrics; }
        List<Long> getValues() { return mValues; }
    }
}
