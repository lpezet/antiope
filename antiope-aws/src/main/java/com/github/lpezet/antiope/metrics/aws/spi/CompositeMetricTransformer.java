/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.metrics.MetricType;

/**
 * @author Luc Pezet
 *
 */
public class CompositeMetricTransformer implements IMetricTransformer {
	
	private Set<IMetricTransformer> mMetricTransformers = new HashSet<IMetricTransformer>();
	private volatile Set<IMetricTransformer> mReadOnly;
	

	@Override
	public List<MetricDatum> toMetricData(MetricType pMetricType, Request<?> pRequest, Response<?> pResponse) {
		List<MetricDatum> oResults = new ArrayList<MetricDatum>();
		for (IMetricTransformer oTransformer : mReadOnly) {
			if (oTransformer.canHandle(pMetricType, pRequest, pResponse))
				oResults.addAll( oTransformer.toMetricData(pMetricType, pRequest, pResponse) );
		}
		return oResults;
	}

	@Override
	public boolean canHandle(MetricType pMetricType, Request<?> pRequest, Response<?> pResponse) {
		for (IMetricTransformer oTransformer : mReadOnly)
			if (oTransformer.canHandle(pMetricType, pRequest, pResponse)) return true;
		return false;
	}
	
	private void syncReadOnly() {
        mReadOnly = Collections.unmodifiableSet(new HashSet<IMetricTransformer>(mMetricTransformers));
    }
	
	public CompositeMetricTransformer with(IMetricTransformer... pTransformers) {
		synchronized(mMetricTransformers) {
            boolean added = mMetricTransformers.addAll(Arrays.asList( pTransformers ));
            if (added)
                syncReadOnly();
            return this;
        }
	}
	
	public boolean addMetricsCollector(IMetricTransformer pCollector) {
        synchronized(mMetricTransformers) {
            boolean added = mMetricTransformers.add(pCollector);
            if (added)
                syncReadOnly();
            return added;
        }
    }
    public <T extends IMetricTransformer> boolean addMetricsCollectors(Collection<T> pTransformers) {
        synchronized(mMetricTransformers) {
            boolean added = mMetricTransformers.addAll(pTransformers);
            if (added)
                syncReadOnly();
            return added;
        }
    }
    public <T extends IMetricTransformer> void setMetricsCollectors(Collection<T> pTransformers) {
        synchronized(mMetricTransformers) {
            if (pTransformers == null || pTransformers.size() == 0) {
                if (mMetricTransformers.size() == 0)
                    return;
                if (pTransformers == null)
                	pTransformers = Collections.emptyList();
            }
            mMetricTransformers.clear();
            if (!addMetricsCollectors(pTransformers)) {
                syncReadOnly(); // avoid missing sync
            }
        }
    }
    public boolean removeMetricsCollectors(IMetricTransformer type) {
        synchronized(mMetricTransformers) {
            boolean removed = mMetricTransformers.remove(type);
            if (removed)
                syncReadOnly();
            return removed;
        }
    }
    public Set<IMetricTransformer> collectors() {
        return mReadOnly;
    }

}
