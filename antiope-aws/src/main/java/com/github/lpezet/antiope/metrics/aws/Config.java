/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws;

/**
 * @author Luc Pezet
 *
 */
public class Config {
	
	private CloudWatchConfig mCloudWatchConfig;
	private MetricsConfig mMetricsConfig;
	

	public CloudWatchConfig getCloudWatchConfig() {
		return mCloudWatchConfig;
	}

	public void setCloudWatchConfig(CloudWatchConfig pCloudWatchConfig) {
		mCloudWatchConfig = pCloudWatchConfig;
	}
	
	public Config withCloudWatchConfig(CloudWatchConfig pConfig) {
		mCloudWatchConfig = pConfig;
		return this;
	}
	
	public MetricsConfig getMetricsConfig() {
		return mMetricsConfig;
	}

	public void setMetricsConfig(MetricsConfig pMetricsConfig) {
		mMetricsConfig = pMetricsConfig;
	}
	
	public Config withMetricsConfig(MetricsConfig pConfig) {
		mMetricsConfig = pConfig;
		return this;
	}
		
}
