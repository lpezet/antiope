/*
 * Copyright 2011-2013 Amazon Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lpezet.antiope2.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

/**
 * @author luc
 *
 */
public class TimingInfoFullSupport extends TimingInfo {

	private final Map<String, List<TimingInfo>> mSubMeasurementsByName = new HashMap<String, List<TimingInfo>>();
    private final Map<String, Number> mCountersByName = new HashMap<String, Number>();

    /**
     * A private ctor to facilitate the deprecation of using millisecond and
     * migration to using nanosecond for timing measurement.
     * 
     * @param pStartEpochTimeMilli start time since epoch in millisecond
     * @param pStartTimeNano start time in nanosecond
     * @param pEndTimeNano end time in nanosecond; or null if not known
     * 
     * @see TimingInfo#startTimingFullSupport()
     * @see TimingInfo#startTimingFullSupport(long)
     * @see TimingInfo#newTimingInfoFullSupport(long, long)
     * @see TimingInfo#newTimingInfoFullSupport(long, long, long)
     */
    TimingInfoFullSupport(Long pStartEpochTimeMilli, long pStartTimeNano, Long pEndTimeNano) {
        super(pStartEpochTimeMilli, pStartTimeNano, pEndTimeNano);
    }

    @Override
    public void addSubMeasurement(String pSubMeasurementName, TimingInfo pTI) {
        List<TimingInfo> timings = mSubMeasurementsByName.get(pSubMeasurementName);
        if (timings == null) {
            timings = new ArrayList<TimingInfo>();
            mSubMeasurementsByName.put(pSubMeasurementName, timings);
        }
        if (pTI.isEndTimeKnown()) {
            timings.add(pTI);
        } else {
            LogFactory.getLog(getClass()).debug(
                "Skip submeasurement timing info with no end time for "
                + pSubMeasurementName);
        }
    }

    @Override
    public TimingInfo getSubMeasurement(String pSubMeasurementName) {
        return getSubMeasurement(pSubMeasurementName, 0);
    }

    @Override
    public TimingInfo getSubMeasurement(String pSubMesurementName, int pIndex) {

        List<TimingInfo> timings = mSubMeasurementsByName.get(pSubMesurementName);
        if (pIndex < 0 || timings == null || timings.size() == 0
                || pIndex >= timings.size()) {
            return null;
        }

        return timings.get(pIndex);
    }

    @Override
    public TimingInfo getLastSubMeasurement(String pSubMeasurementName) {

        if (mSubMeasurementsByName == null || mSubMeasurementsByName.size() == 0) {
            return null;
        }

        List<TimingInfo> timings = mSubMeasurementsByName.get(pSubMeasurementName);
        if (timings == null || timings.size() == 0) {
            return null;
        }

        return timings.get(timings.size() - 1);
    }

    @Override
    public List<TimingInfo> getAllSubMeasurements(String pSubMeasurementName) {
        return mSubMeasurementsByName.get(pSubMeasurementName);
    }

    @Override
    public Map<String, List<TimingInfo>> getSubMeasurementsByName() {
        return mSubMeasurementsByName;
    }

    @Override
    public Number getCounter(String key) {
        return mCountersByName.get(key);
    }

    @Override
    public Map<String, Number> getAllCounters() {
        return mCountersByName;
    }

    @Override
    public void setCounter(String key, long count) {
        mCountersByName.put(key, count);
    }

    @Override
    public void incrementCounter(String key) {

        int count = 0;
        Number counter = getCounter(key);

        if (counter != null) {
            count = counter.intValue();
        }

        setCounter(key, ++count);
    }

}
