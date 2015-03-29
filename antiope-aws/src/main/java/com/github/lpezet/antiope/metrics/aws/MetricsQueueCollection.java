/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws;

import java.util.Queue;
import java.util.Set;

import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.metrics.IMetrics;
import com.github.lpezet.antiope.metrics.IMetricsCollector;
import com.github.lpezet.antiope.metrics.MetricType;
import com.github.lpezet.antiope.metrics.aws.spi.IMetricTransformer;

/**
 * 
 * @author Luc Pezet
 */
@ThreadSafe
public class MetricsQueueCollection implements IMetricsCollector {
    protected final static Logger mLogger = LoggerFactory.getLogger(MetricsQueueCollection.class);
    private final Queue<MetricDatum> mQueue;
    private IMetricTransformer mTransformer = IMetricTransformer.NONE;
    private final Set<MetricType> mPredefinedMetrics;
    
    protected MetricsQueueCollection(Config pConfig, Queue<MetricDatum> pQueue) {
        this.mQueue = pQueue;
        mPredefinedMetrics = pConfig.getMetricsConfig().getPredefinedMetrics();
        mTransformer = pConfig.getMetricsConfig().getMetricTransformer();
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
        	//if (!(oType instanceof APIRequestMetrics))
            //    continue;
            if (mLogger.isDebugEnabled()) mLogger.debug("Collecting metric: " + oType);
        	if (!mTransformer.canHandle(oType, pRequest, pResponse))
        		continue;
            for (MetricDatum datum : mTransformer.toMetricData(oType, pRequest, pResponse)) {
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
    //protected PredefinedMetricTransformer getTransformer() { return mTransformer; }
}
