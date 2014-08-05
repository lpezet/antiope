/**
 * The MIT License
 * Copyright (c) 2014 Luc Pezet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
