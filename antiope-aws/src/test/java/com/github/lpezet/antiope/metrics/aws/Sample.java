/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.be.APIWebServiceRequest;
import com.github.lpezet.antiope.metrics.APIRequestMetrics;
import com.github.lpezet.antiope.metrics.IMetricsCollector;

/**
 * @author Luc Pezet
 *
 */
public class Sample extends BaseSample {
	
	private static final String AWS_ACCESS_KEY = "";
	private static final String AWS_SECRET_KEY = "";
	

	static class MyRequest extends APIWebServiceRequest {
		
	}
	
	@Test(timeout=60000)
	public void doIt() throws Exception {
		HttpClient oHttpClient = HttpClients.createDefault();
		APIConfiguration oAPIConfig = new APIConfiguration();
		oAPIConfig.setProfilingEnabled(true);
		SimpleClient oClient = new SimpleClient(oAPIConfig, oHttpClient, getPort());
		
		Config oConfig = new Config();
		CloudWatchConfig oCWConfig = new CloudWatchConfig();
		oConfig.setCloudWatchConfig(oCWConfig);
		
		oCWConfig.setCloudWatchEndPoint("http://localhost:" + getCloudWatchPort());
		oCWConfig.setQueuePollTimeoutMilli(TimeUnit.SECONDS.toMillis(5));
		oCWConfig.setCredentialsProvider(new StaticCredentialsProvider(new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)));
		
		MetricsConfig oMConfig = new MetricsConfig();
		oConfig.setMetricsConfig(oMConfig);
		oMConfig.setMetricNameSpace("Antiope/Test");
		// Reset default metrics to just Client Execute Time
		oMConfig.getMetricsRegistry().setMetricTypes(Arrays.asList( APIRequestMetrics.ClientExecuteTime));
		//oMConfig.getMetricsRegistry().setMetricTypes(new ArrayList<MetricType>());
		oMConfig.setMachineMetricExcluded(true);
		IMetricsCollector oMetricsCollector = new DefaultMetricsCollectorFactory(oConfig).getInstance();
		oClient.setMetricsCollector(oMetricsCollector);
		
		for (int i = 0; i < 20; i++) {
			Thread.sleep(1000);
			oClient.getIt();
		}
	}
}
