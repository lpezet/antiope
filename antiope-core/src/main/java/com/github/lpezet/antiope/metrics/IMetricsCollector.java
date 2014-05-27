/**
 * 
 */
package com.github.lpezet.antiope.metrics;

import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;

/**
 * @author luc
 *
 */
public interface IMetricsCollector {

	public void collectMetrics(Request<?> request, Response<?> response);
    public boolean isEnabled();
}
