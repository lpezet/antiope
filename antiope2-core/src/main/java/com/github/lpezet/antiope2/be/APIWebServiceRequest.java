/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * 
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.github.lpezet.antiope2.be;

import java.util.HashMap;
import java.util.Map;

import com.github.lpezet.antiope2.dao.IMetricsAware;
import com.github.lpezet.antiope2.metrics.IMetrics;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;


/**
 * @author luc
 *
 */
public abstract class APIWebServiceRequest implements IMetricsAware {
	
	private IAPICredentials mCredentials;
	
	private String mServiceName;
	
	private IMetrics mMetrics;
	
	/**
     * A request metric collector used for this specific service request; or
     * null if there is none.  This collector always takes precedence over the
     * ones specified at the http client level.
     */
    private IMetricsCollector mMetricsCollector;
    
    @Override
    public IMetrics getMetrics() {
    	return mMetrics;
    }
    
    public void setMetrics(IMetrics pMetrics) {
    	mMetrics = pMetrics;
    };
    
    public String getServiceName() {
		return mServiceName;
	}
    
    public void setServiceName(String pServiceName) {
		mServiceName = pServiceName;
	}
    
	public IAPICredentials getCredentials() {
		return mCredentials;
	}

	public void setCredentials(IAPICredentials pCredentials) {
		mCredentials = pCredentials;
	}
	
	public void setMetricsCollector(IMetricsCollector pMetricsCollector) {
		mMetricsCollector = pMetricsCollector;
	}
	
	public IMetricsCollector getMetricsCollector() {
		return mMetricsCollector;
	}

	/**
     * Internal only method for accessing private, internal request parameters.
     * Not intended for direct use by callers.
     *
     * @return private, internal request parameter information.
     */
    public Map<String, String> copyPrivateRequestParameters() {
        return new HashMap<String, String>();
    }
}
