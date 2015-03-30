/*
 * Copyright 2011-2013 Amazon Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lpezet.antiope2.dao;

import com.github.lpezet.antiope2.be.IAPIConfiguration;
import com.github.lpezet.antiope2.be.IAPICredentials;
import com.github.lpezet.antiope2.metrics.IMetrics;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;

/**
 * @author luc
 *
 */
public class ExecutionContext {

	private final IMetrics mMetrics;
	private final IMetricsCollector mMetricsCollector;
	private IAPIConfiguration mAPIConfiguration;
	private IAPICredentials mCredentials;
	private String contextUserAgent;
	
	public ExecutionContext() {
		this(null,null);
	}
	
	public ExecutionContext(IMetrics pMetrics) {
		this(pMetrics, null);
	}
	
	public ExecutionContext(IMetrics pMetrics, IMetricsCollector pMetricsCollector) {
		mMetrics = pMetrics;
		mMetricsCollector = pMetricsCollector;
	}
	
	public IAPIConfiguration getAPIConfiguration() {
		return mAPIConfiguration;
	}
	
	public void setAPIConfiguration(IAPIConfiguration pAPIConfiguration) {
		mAPIConfiguration = pAPIConfiguration;
	}

	public IAPICredentials getCredentials() {
		return mCredentials;
	}

	public void setCredentials(IAPICredentials pCredentials) {
		mCredentials = pCredentials;
	}
	
	public IMetrics getMetrics() {
		return mMetrics;
	}
	
	public String getContextUserAgent() {
		return contextUserAgent;
	}

	public void setContextUserAgent(String pContextUserAgent) {
		contextUserAgent = pContextUserAgent;
	}
	
	public IMetricsCollector getMetricsCollector() {
		return mMetricsCollector;
	}
	
}
