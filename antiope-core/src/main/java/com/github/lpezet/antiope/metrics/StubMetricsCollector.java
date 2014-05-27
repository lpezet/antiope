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
public class StubMetricsCollector implements IMetricsCollector {
	
	private static final StubMetricsCollector INSTANCE = new StubMetricsCollector();
	
	public static StubMetricsCollector getInstance() {
		return INSTANCE;
	}

	@Override
	public void collectMetrics(Request<?> pRequest, Response<?> pResponse) {
		// nop
	}
	
	@Override
	public boolean isEnabled() {
		return false;
	}
}
