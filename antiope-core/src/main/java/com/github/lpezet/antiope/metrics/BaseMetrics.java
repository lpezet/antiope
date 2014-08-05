package com.github.lpezet.antiope.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseMetrics implements IMetrics {

	/* Stores some key value pairs. */
    private final Map<String, List<Object>> mProperties = new HashMap<String, List<Object>>();
    
    /* A map to store events that are being profiled. */
    private final Map<String, TimingInfo> mEventsBeingProfiled = new HashMap<String, TimingInfo>();
    /* Latency Logger */
    private static final Logger LATENCY_LOGGER = LoggerFactory.getLogger("api.latency");
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMetrics.class);
    private static final Object KEY_VALUE_SEPARATOR = "=";
    private static final Object COMMA_SEPARATOR = ", ";
    
    protected final TimingInfo timingInfo;

    public BaseMetrics() {
        this.timingInfo = TimingInfo.startTimingFullSupport();
    }

    protected BaseMetrics(TimingInfo timingInfo) {
        this.timingInfo = timingInfo;
    }

    public final TimingInfo getTimingInfo() {
        return timingInfo;
    }
    
    /**
     * Start an event which will be timed. The startTime and endTime are added
     * to timingInfo only after endEvent is called. For every startEvent there
     * should be a corresponding endEvent. If you start the same event without
     * ending it, this will overwrite the old event. i.e. There is no support
     * for recursive events yet. Having said that, if you start and end an event
     * in that sequence multiple times, all events are logged in timingInfo in
     * that order.
     * 
     * This feature is enabled if the system property
     * "tsg.tripfilmsws.sdk.enableRuntimeProfiling" is set, or if a
     * {@link RequestMetricCollector} is in use either at the request, web service
     * client, or TSG SDK level.
     * 
     * @param eventName
     *            - The name of the event to start
     * 
     * @see TSGSdkMetrics
     */
    @Override
    public void startEvent(String eventName) {
        /* This will overwrite past events */
        mEventsBeingProfiled.put // ignoring the wall clock time
            (eventName, TimingInfo.startTimingFullSupport(System.nanoTime()));
    }

    @Override
    public void startEvent(MetricType f) {
        startEvent(f.name());
    }

    /**
     * End an event which was previously started. Once ended, log how much time
     * the event took. It is illegal to end an Event that was not started. It is
     * good practice to endEvent in a finally block. See Also startEvent.
     * 
     * @param eventName
     *            - The name of the event to start
     */
    @Override
    public void endEvent(String eventName) {
        TimingInfo event = mEventsBeingProfiled.get(eventName);
        /* Somebody tried to end an event that was not started. */
        if (event == null) {
            LOGGER.warn
                ("Trying to end an event which was never started: " + eventName);
            return;
        }
        event.endTiming();
        this.timingInfo.addSubMeasurement(
            eventName,
            TimingInfo.unmodifiableTimingInfo(
                event.getStartTimeNano(),
                event.getEndTimeNano()));
    }

    @Override
    public void endEvent(MetricType f) {
        endEvent(f.name());
    }

    /**
     * Add 1 to an existing count for a given event. If the count for that event
     * does not exist, then it creates one and initializes it to 1.
     * 
     * This feature is enabled if the system property
     * "tsg.tripfilmsws.sdk.enableRuntimeProfiling" is set, or if a
     * {@link RequestMetricCollector} is in use either at the request, web service
     * client, or TSG SDK level.
     * 
     * @param event
     *            - The name of the event to count
     */
    @Override
    public void incrementCounter(String event) {
        timingInfo.incrementCounter(event);
    }

    @Override
    public void incrementCounter(MetricType f) {
        incrementCounter(f.name());
    }
    
    @Override
    public void setCounter(String counterName, long count) {
        timingInfo.setCounter(counterName, count);
    }

    @Override
    public void setCounter(MetricType f, long count) {
        setCounter(f.name(), count);
    }
    
    /**
     * Add a property. If you add the same property more than once, it stores
     * all values a list.
     * 
     * This feature is enabled if the system property
     * "tsg.tripfilmsws.sdk.enableRuntimeProfiling" is set, or if a
     * {@link RequestMetricCollector} is in use either at the request, web service
     * client, or TSG SDK level.
     * 
     * @param propertyName
     *            The name of the property
     * @param value
     *            The property value
     */
    @Override
    public void addProperty(String propertyName, Object value) {
        List<Object> propertyList = mProperties.get(propertyName);
        if (propertyList == null) {
            propertyList = new ArrayList<Object>();
            mProperties.put(propertyName, propertyList);
        }
        
        propertyList.add(value);
    }

    @Override
    public void addProperty(MetricType f, Object value) {
        addProperty(f.name(), value);
    }

    @Override
    public void log() {
        StringBuilder builder = new StringBuilder();

        for (Entry<String, List<Object>> entry : mProperties.entrySet()) {
            keyValueFormat(entry.getKey(), entry.getValue(), builder);
        }

        for (Entry<String, Number> entry : timingInfo.getAllCounters().entrySet()) {
            keyValueFormat(entry.getKey(), entry.getValue(), builder);
        }

        for (Entry<String, List<TimingInfo>> entry : timingInfo.getSubMeasurementsByName().entrySet()) {
            keyValueFormat(entry.getKey(), entry.getValue(), builder);
        }

        LATENCY_LOGGER.info(builder.toString());
    }

    private void keyValueFormat(Object key, Object value, StringBuilder builder) {
        builder.append(key).append(KEY_VALUE_SEPARATOR).append(value).append(COMMA_SEPARATOR);
    }

    @Override
    public List<Object> getProperty(String propertyName){
    	return mProperties.get(propertyName);
    }

    @Override
    public List<Object> getProperty(MetricType f){
        return getProperty(f.name());
    }
}
