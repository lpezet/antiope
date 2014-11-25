/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws.spi;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.metrics.MetricType;
import com.github.lpezet.antiope.metrics.TimingInfo;

/**
 * @author Luc Pezet
 *
 */
public interface IMetricTransformer {
	/**
     * Returns a list of metric datum for the metrics collected for the given
     * request/response, or null if this transformer does not recognize the
     * specific input metric type.
     * <p>
     * Note returning an empty list means the transformer recognized the metric
     * type but concluded there is no metrics to be generated for it.
     * 
     * @param pMetricType
     *            the predefined metric type
     */
    public List<MetricDatum> toMetricData(MetricType pMetricType, Request<?> pRequest, Response<?> pResponse);
    
    public boolean canHandle(MetricType pMetricType, Request<?> pRequest, Response<?> pResponse);
    
    /** A convenient instance of a no-op request metric transformer. */
    public static final IMetricTransformer NONE = new IMetricTransformer() {
		
		@Override
		public List<MetricDatum> toMetricData(MetricType pMetricType, Request<?> pRequest, Response<?> pResponse) {
			return Collections.emptyList();
		}
		
		@Override
		public boolean canHandle(MetricType pMetricType, Request<?> pRequest, Response<?> pResponse) {
			return false;
		}
	};

    /** Common utilities for implementing this SPI. */
    public static enum Utils {
        ;

        public static long endTimeMilli(TimingInfo ti) {
            Long endTimeMilli = ti.getEndEpochTimeMilliIfKnown();
            return endTimeMilli == null ? System.currentTimeMillis() : endTimeMilli.longValue();
        }

        public static Date endTimestamp(TimingInfo ti) {
            return new Date(endTimeMilli(ti));
        }
    }
}
