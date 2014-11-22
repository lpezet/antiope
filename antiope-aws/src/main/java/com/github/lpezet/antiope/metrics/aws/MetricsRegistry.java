/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.lpezet.antiope.metrics.APIRequestMetrics;
import com.github.lpezet.antiope.metrics.MetricType;


/**
 * @author Luc Pezet
 *
 */
public class MetricsRegistry {
    private final Set<MetricType> mMetricTypes = new HashSet<MetricType>();
    private volatile Set<MetricType> mReadOnly;

    MetricsRegistry() {
        mMetricTypes.add(APIRequestMetrics.ClientExecuteTime);
        mMetricTypes.add(APIRequestMetrics.Exception);
        mMetricTypes.add(APIRequestMetrics.ThrottleException);
        mMetricTypes.add(APIRequestMetrics.HttpClientRetryCount);
        mMetricTypes.add(APIRequestMetrics.HttpRequestTime);
        mMetricTypes.add(APIRequestMetrics.RequestCount);
//        metricTypes.add(Field.RequestSigningTime);
//        metricTypes.add(Field.ResponseProcessingTime);
        mMetricTypes.add(APIRequestMetrics.RetryCount);
        mMetricTypes.add(APIRequestMetrics.HttpClientSendRequestTime);
        mMetricTypes.add(APIRequestMetrics.HttpClientReceiveResponseTime);
        mMetricTypes.add(APIRequestMetrics.HttpClientPoolAvailableCount);
        mMetricTypes.add(APIRequestMetrics.HttpClientPoolLeasedCount);
        mMetricTypes.add(APIRequestMetrics.HttpClientPoolPendingCount);
        //metricTypes.add(AWSServiceMetrics.HttpClientGetConnectionTime);
        syncReadOnly();
    }

    private void syncReadOnly() {
        mReadOnly = Collections.unmodifiableSet(new HashSet<MetricType>(mMetricTypes));
    }

    public boolean addMetricType(MetricType type) {
        synchronized(mMetricTypes) {
            boolean added = mMetricTypes.add(type);
            if (added)
                syncReadOnly();
            return added;
        }
    }
    public <T extends MetricType> boolean addMetricTypes(Collection<T> types) {
        synchronized(mMetricTypes) {
            boolean added = mMetricTypes.addAll(types);
            if (added)
                syncReadOnly();
            return added;
        }
    }
    public <T extends MetricType> void setMetricTypes(Collection<T> types) {
        synchronized(mMetricTypes) {
            if (types == null || types.size() == 0) {
                if (mMetricTypes.size() == 0)
                    return;
                if (types == null)
                    types = Collections.emptyList();
            }
            mMetricTypes.clear();
            if (!addMetricTypes(types)) {
                syncReadOnly(); // avoid missing sync
            }
        }
    }
    public boolean removeMetricType(MetricType type) {
        synchronized(mMetricTypes) {
            boolean removed = mMetricTypes.remove(type);
            if (removed)
                syncReadOnly();
            return removed;
        }
    }
    public Set<MetricType> predefinedMetrics() {
        return mReadOnly;
    }
}
