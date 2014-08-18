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
