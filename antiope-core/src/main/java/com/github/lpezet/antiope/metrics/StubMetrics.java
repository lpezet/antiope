/**
 * 
 */
package com.github.lpezet.antiope.metrics;

import java.util.Collections;
import java.util.List;

/**
 * @author luc
 *
 */
public class StubMetrics implements IMetrics {

	@Override
	public TimingInfo getTimingInfo() { return TimingInfo.startTiming(); }
	
	@Override
	public void startEvent(String pEventName) {}

	@Override
	public void startEvent(MetricType pF) {}

	@Override
	public void endEvent(String pEventName) {}

	@Override
	public void endEvent(MetricType pF) {}

	@Override
	public void incrementCounter(String pEvent) {}

	@Override
	public void incrementCounter(MetricType pF) {}

	@Override
	public void setCounter(String pCounterName, long pCount) {}

	@Override
	public void setCounter(MetricType pF, long pCount) {}

	@Override
	public void addProperty(String pPropertyName, Object pValue) {}

	@Override
	public void addProperty(MetricType pF, Object pValue) {}

	@Override
	public void log() {}

	@Override
	public List<Object> getProperty(String pPropertyName) { return Collections.emptyList(); }

	@Override
	public List<Object> getProperty(MetricType pF) { return Collections.emptyList(); }
	

}
