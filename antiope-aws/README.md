Antiope-AWS
===========

[IMetricsCollector](https://github.com/lpezet/antiope/blob/master/antiope-core/src/main/java/com/github/lpezet/antiope/metrics/IMetricsCollector.java) implementation using AWS CloudWatch to keep track of metrics.


Documentation
-------------

### Predefined metrics

It comes with the same predefined metrics implemented in AWS Java SDK. The following metrics are handled automatically and send to CloudWatch using the same metric names:
* **ClientExecuteTime**: total number of milliseconds taken for a request/response including the time taken to execute the request handlers, round trip to API, and the time taken to execute the response handlers.
* **Exception**
* **ThrottleException**
* **HttpClientRetryCount**: Number of retries of the underlying http client library in sending a request to API.
* **HttpRequestTime**: Number of milliseconds taken for a request/response round trip to API.
* **RequestCount**: Number of requests to API.
* **RetryCount**: Number of retries of API SDK sending a request to API. 
* **HttpClientSendRequestTime**: Time taken to send a request to API by the http client library, excluding any retry.
* **HttpClientReceiveResponseTime**: Time taken to receive a response from API by the http client library, excluding any retry.
* **HttpClientPoolAvailableCount**: The number of idle persistent connections. Reference: (https://hc.apache.org/httpcomponents-core-ga/httpcore/apidocs/org/apache/http/pool/PoolStats.html)
* **HttpClientPoolLeasedCount**: The number of persistent connections tracked by the connection manager currently being used to execute requests. Reference: (https://hc.apache.org/httpcomponents-core-ga/httpcore/apidocs/org/apache/http/pool/PoolStats.html).
* **HttpClientPoolPendingCount**: The number of connection requests being blocked awaiting a free connection. Reference: (https://hc.apache.org/httpcomponents-core-ga/httpcore/apidocs/org/apache/http/pool/PoolStats.html).

### Usage

Here's a snippet of code to start using CloudWatch metrics collector:
```java
...
APIConfiguration oAPIConfig = new APIConfiguration();
oAPIConfig.setProfilingEnabled(true);
...
MyAPIClient oClient = new MyAPIClient(oAPIConfig, ...);

// #######################################
// Antiope-AWS section
// #######################################
Config oConfig = new Config();

// CloudWatch configuration: most important here is the AWS Credentials.
CloudWatchConfig oCWConfig = new CloudWatchConfig();
oConfig.setCloudWatchConfig(oCWConfig);
oCWConfig.setQueuePollTimeoutMilli(TimeUnit.MINUTES.toMillis(1));
oCWConfig.setCredentialsProvider(new StaticCredentialsProvider(new BasicAWSCredentials("XXX", "XXX")));

// Metrics configuration: only setting the NameSpace is is required.
MetricsConfig oMConfig = new MetricsConfig();
oConfig.setMetricsConfig(oMConfig);
oMConfig.setMetricNameSpace("Antiope/Test");

IMetricsCollector oMetricsCollector = new DefaultMetricsCollectorFactory(oConfig).getInstance();

oClient.setMetricsCollector(oMetricsCollector);
```

For more information about API Client instrumentation and metric collection, check out this [blog post](TODO).

Installation
------------

#### pom.xml

```xml
<repositories>
	<repository>
		<id>lpezet-snapshot</id>
		<url>https://repository-lpezet.forge.cloudbees.com/snapshot/</url>
		<name>LPezet Snapshot Repo</name>
		<snapshots>
			<enabled>true</enabled>
		</snapshots>
		<releases>
			<enabled>false</enabled>
		</releases>
	</repository>
	<repository>
		<id>lpezet-release</id>
		<url>https://repository-lpezet.forge.cloudbees.com/release/</url>
		<name>LPezet Snapshot Repo</name>
		<snapshots>
			<enabled>false</enabled>
		</snapshots>
		<releases>
			<enabled>true</enabled>
		</releases>
	</repository>
</repositories>
```

License
-------

See [LICENSE](src/main/resources/META-INF/LICENSE) file.
