package com.github.lpezet.antiope.metrics;

import java.util.List;

public interface IMetrics {

	public TimingInfo getTimingInfo();
	public void startEvent(String eventName);
    public void startEvent(MetricType f);
    public void endEvent(String eventName);
    public void endEvent(MetricType f);
    public void incrementCounter(String event);
    public void incrementCounter(MetricType f);
    public void setCounter(String counterName, long count);
    public void setCounter(MetricType f, long count);
    public void addProperty(String propertyName, Object value);
    public void addProperty(MetricType f, Object value);
    public void log();
    public List<Object> getProperty(String propertyName);
    public List<Object> getProperty(MetricType f);
    
}
