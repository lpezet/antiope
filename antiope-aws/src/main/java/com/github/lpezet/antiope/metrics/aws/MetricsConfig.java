/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws;

import java.util.Set;

import com.amazonaws.regions.Regions;
import com.github.lpezet.antiope.metrics.MetricType;

/**
 * @author Luc Pezet
 *
 */
public class MetricsConfig {
	
	public static final String DEFAULT_METRIC_NAMESPACE = "APISDK/Java";

	private boolean mMachineMetricExcluded;
	private String mMetricNameSpace;
	private boolean mPerHostMetricEnabled;
	private boolean mSingleMetricNamespace;
	private String mHostMetricName;
	private String mJvmMetricName;
	private Regions mRegion;
	private MetricsRegistry mMetricsRegistry = new MetricsRegistry();
	
	public boolean isMachineMetricExcluded() {
		return mMachineMetricExcluded;
	}
	public void setMachineMetricExcluded(boolean pMachineMetricExcluded) {
		mMachineMetricExcluded = pMachineMetricExcluded;
	}
	public String getMetricNameSpace() {
		return mMetricNameSpace;
	}
	public void setMetricNameSpace(String pMetricNameSpace) {
		mMetricNameSpace = pMetricNameSpace;
	}
	public boolean isPerHostMetricEnabled() {
		return mPerHostMetricEnabled;
	}
	public void setPerHostMetricEnabled(boolean pPerHostMetricEnabled) {
		mPerHostMetricEnabled = pPerHostMetricEnabled;
	}
	public boolean isSingleMetricNamespace() {
		return mSingleMetricNamespace;
	}
	public void setSingleMetricNamespace(boolean pSingleMetricNamespace) {
		mSingleMetricNamespace = pSingleMetricNamespace;
	}
	public String getHostMetricName() {
		return mHostMetricName;
	}
	public void setHostMetricName(String pHostMetricName) {
		mHostMetricName = pHostMetricName;
	}
	public String getJvmMetricName() {
		return mJvmMetricName;
	}
	public void setJvmMetricName(String pJvmMetricName) {
		mJvmMetricName = pJvmMetricName;
	}
	public MetricsConfig withJvmMetricName(String pValue) {
		mJvmMetricName = pValue;
		return this;
	}
	public Regions getRegion() {
		return mRegion;
	}
	public void setRegion(Regions pRegion) {
		mRegion = pRegion;
	}
	public MetricsConfig withRegion(Regions pValue) {
		mRegion = pValue;
		return this;
	}
	public MetricsRegistry getMetricsRegistry() {
		return mMetricsRegistry;
	}
	public void setMetricsRegistry(MetricsRegistry pMetricsRegistry) {
		mMetricsRegistry = pMetricsRegistry;
	}
	public MetricsConfig withMetricsRegistry(MetricsRegistry pValue) {
		mMetricsRegistry = pValue;
		return this;
	}
	public Set<MetricType> getPredefinedMetrics() {
		return mMetricsRegistry.predefinedMetrics();
	}
}
