/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.github.lpezet.antiope.metrics.aws;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.regions.Regions;

/**
 * The default AWS SDK metric collector factory.  This class is instantiated
 * via reflection if the default AWS SDK metrics is enabled via the
 * system property {@link SDKGlobalConfiguration#DEFAULT_METRICS_SYSTEM_PROPERTY}.
 */
public class DefaultMetricsCollectorFactory implements ThreadedMetricsCollector.Factory {
    
	private Config mConfig;
	
	/**
     * Returns a instance of the Amazon CloudWatch request metric collector either by
     * starting up a new one or returning an existing one if it's already
     * started; null if any failure.
     */
    @Override
    public ThreadedMetricsCollector getInstance() {
    	Regions region = mConfig.getMetricsConfig().getRegion();
        Integer oQSize = mConfig.getCloudWatchConfig().getMetricQueueSize();
        Long oTimeoutMilli = mConfig.getCloudWatchConfig().getQueuePollTimeoutMilli();
        CloudWatchConfig oCloudWatchConfig = new CloudWatchConfig(mConfig.getCloudWatchConfig());
        if (mConfig.getCloudWatchConfig().getCredentialsProvider() != null)
            oCloudWatchConfig.setCredentialsProvider(mConfig.getCloudWatchConfig().getCredentialsProvider());
        if (region != null) {
            String endPoint = "monitoring." + region.getName() + ".amazonaws.com";
            oCloudWatchConfig.setCloudWatchEndPoint(endPoint);
        }
        if (oQSize != null)
            oCloudWatchConfig.setMetricQueueSize(oQSize.intValue());
        if (oTimeoutMilli != null)
            oCloudWatchConfig.setQueuePollTimeoutMilli(oTimeoutMilli.longValue());
        Config oConfig = new Config();
        oConfig.setCloudWatchConfig(oCloudWatchConfig);
        oConfig.setMetricsConfig(mConfig.getMetricsConfig());
        
        //MetricsCollectorSupport.startSingleton(oConfig);
        //return MetricsCollectorSupport.getInstance();
        MetricsCollectorSupport oCollector = new MetricsCollectorSupport(oConfig);
        oCollector.start();
        return oCollector;
    }
    
    public DefaultMetricsCollectorFactory(Config pConfig) {
		mConfig = pConfig;
	}
}
