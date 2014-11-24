/*
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

import java.util.concurrent.TimeUnit;

import org.apache.http.annotation.NotThreadSafe;

import com.amazonaws.auth.AWSCredentialsProvider;

/**
 * Configuration for the default AWS SDK collection implementation. This class
 * is not intended to be used directly by client code except for cases where the
 * default behavior of the internal Amazon CloudWatch collector implementation
 * needs to be customized.
 * <p>
 * Example:
 * 
 * <pre>
 * /**
 *  * My custom Request Metric Collector by extending from the internal Amazon CloudWatch
 *  * implementation.
 *  &#42;/
 * static class MyCloudWatchMetricCollector extends
 *         CloudWatchRequestMetricCollector {
 *     MyCloudWatchMetricCollector(CloudWatchMetricConfig config) {
 *         super(config);
 *     }
 * }
 * MyCloudWatchMetricCollector myCollector = new MyCloudWatchMetricCollector(
 *         new CloudWatchMetricConfig()
 *                 .withQueuePollTimeoutMilli(60000)
 *                 .withMetricQueueSize(1000)
 *                 .withCredentialsProvider(
 *                         new DefaultAWSCredentialsProviderChain())
 *                 .withCloudWatchEndPoint(&quot;monitoring.us-west-2.amazonaws.com&quot;)
 *                 .withPredefinedMetrics(
 *                         new HashSet&lt;Field&gt;(Arrays.asList(Field.HttpRequestTime,
 *                                 Field.ResponseProcessingTime))));
 * myCollector.start();
 * // Enable the AWS SDK level request metric collection with a custom collector
 * AwsSdkMetrics.setRequestMetricCollector(myCollector);
 * </pre>
 * 
 * 
 */
@NotThreadSafe
public class CloudWatchConfig {
    static final String NAMESPACE_DELIMITER = "/";
    /**
     * Maximum number of metric data that Amazon CloudWatch can
     * accept in a single request
     */
    static final int MAX_METRICS_DATUM_SIZE = 20;
    /**
     * Default metrics queue size. If the queue size
     * exceeds this value, then excessive metrics will be dropped to prevent
     * resource exhaustion.
     */
    public static final int DEFAULT_METRICS_QSIZE = 1000;
    /**
     * Default timeout in millisecond for queue polling.  Set to one-minute
     * which is the finest granularity of Amazon CloudWatch. 
     */
    public static final int DEFAULT_QUEUE_POLL_TIMEOUT_MILLI = (int)TimeUnit.MINUTES.toMillis(1);

    /** Credentials for the uploader to communicate with Amazon CloudWatch */
    private AWSCredentialsProvider mCredentialsProvider;

    /**
     * Number of milliseconds to wait before the polling of the metrics queue
     * times out.
     */
    private long mQueuePollTimeoutMilli = DEFAULT_QUEUE_POLL_TIMEOUT_MILLI;

    /** 
     * Endpoint for Amazon CloudWatch where the metric data can be uploaded;
     * or null if the default endpoint is to be used.
     */
    private String mCloudWatchEndPoint;
    
    private int mMetricQueueSize = DEFAULT_METRICS_QSIZE;

    public CloudWatchConfig() {
    	
    }
    
    public CloudWatchConfig(CloudWatchConfig pSource) {
		withCloudWatchEndPoint(pSource.getCloudWatchEndPoint())
		.withCredentialsProvider(pSource.getCredentialsProvider())
		.withMetricQueueSize(pSource.getMetricQueueSize())
		.withQueuePollTimeoutMilli(pSource.getQueuePollTimeoutMilli());
	}

	/**
     * Returns the credential provider that holds the credentials to connect to
     * Amazon CloudWatch.
     */
    public AWSCredentialsProvider getCredentialsProvider() {
        return mCredentialsProvider;
    }

    /**
     * Sets the credential provider to the given provider. This credential
     * provider is used by the uploader thread to connect to Amazon CloudWatch.
     */
    public void setCredentialsProvider(AWSCredentialsProvider pCredentialsProvider) {
        this.mCredentialsProvider = pCredentialsProvider;
    }

    public CloudWatchConfig withCredentialsProvider(AWSCredentialsProvider pCredentialsProvider) {
        setCredentialsProvider(pCredentialsProvider);
        return this;
    }

    /**
     * Returns the metrics queue polling timeout in millisecond.
     */
    public long getQueuePollTimeoutMilli() {
        return mQueuePollTimeoutMilli;
    }

    /**
     * Sets the metric queue polling timeout in millisecond. The default set
     * set to one-minute per the finest granularity of Amazon CloudWatch
     */
    public void setQueuePollTimeoutMilli(long pQueuePollTimeoutMilli) {
        this.mQueuePollTimeoutMilli = pQueuePollTimeoutMilli;
    }

    public CloudWatchConfig withQueuePollTimeoutMilli(long pQueuePollTimeoutMilli) {
        setQueuePollTimeoutMilli(pQueuePollTimeoutMilli);
        return this;
    }

    /**
     * Returns the end point of AmazonCloudWatch to upload the metrics.
     */
    public String getCloudWatchEndPoint() {
        return mCloudWatchEndPoint;
    }

    /**
     * Sets the end point of AmazonCloudWatch to upload the metrics.
     */
    public void setCloudWatchEndPoint(String pCloudWatchEndPoint) {
        this.mCloudWatchEndPoint = pCloudWatchEndPoint;
    }

    public CloudWatchConfig withCloudWatchEndPoint(String pCloudWatchEndPoint) {
        setCloudWatchEndPoint(pCloudWatchEndPoint);
        return this;
    }

    public int getMetricQueueSize() {
        return mMetricQueueSize;
    }

    /**
     * Configure the metric queue size, overriding the default. Must be at
     * least 1.
     * 
     * @see #DEFAULT_METRICS_QSIZE
     */
    public void setMetricQueueSize(int pMetricQueueSize) {
        if (pMetricQueueSize < 1) {
            throw new IllegalArgumentException();
        }
        this.mMetricQueueSize = pMetricQueueSize;
    }

    public CloudWatchConfig withMetricQueueSize(int pMetricQueueSize) {
        setMetricQueueSize(pMetricQueueSize);
        return this;
    }
}
