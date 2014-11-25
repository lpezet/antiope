/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws;

import static com.github.lpezet.antiope.metrics.aws.spi.IMetricTransformer.Utils.endTimestamp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.github.lpezet.antiope.APIClientException;
import com.github.lpezet.antiope.APIServiceException;
import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.dao.DefaultRequest;
import com.github.lpezet.antiope.dao.ExecutionContext;
import com.github.lpezet.antiope.dao.HttpMethodName;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.metrics.APIRequestMetrics;
import com.github.lpezet.antiope.metrics.IMetrics;
import com.github.lpezet.antiope.metrics.IMetricsCollector;
import com.github.lpezet.antiope.metrics.MetricType;
import com.github.lpezet.antiope.metrics.TimingInfo;
import com.github.lpezet.antiope.metrics.aws.Sample.MyRequest;
import com.github.lpezet.antiope.metrics.aws.spi.CompositeMetricTransformer;
import com.github.lpezet.antiope.metrics.aws.spi.Dimensions;
import com.github.lpezet.antiope.metrics.aws.spi.IMetricTransformer;

/**
 * @author Luc Pezet
 *
 */
public class CustomMetricSample extends BaseSample {

	enum MyCustonMetric implements MetricType {
		TotalResultsRequested;
	}
	
	private static class CustomMetricTransformer implements IMetricTransformer {

		@Override
		public List<MetricDatum> toMetricData(MetricType pMetricType, Request<?> pRequest, Response<?> pResponse) {
			if (!(pMetricType instanceof MyCustonMetric)) return Collections.emptyList();
			MyCustonMetric oMetric = (MyCustonMetric) pMetricType;
			switch(oMetric) {
				case TotalResultsRequested:
					return metricOfCount(oMetric, pRequest, pResponse);
				default:
					return Collections.emptyList();
			}
		}

		@Override
		public boolean canHandle(MetricType pMetricType, Request<?> pRequest, Response<?> pResponse) {
			return (pMetricType instanceof MyCustonMetric);
		}
		
		protected List<MetricDatum> metricOfCount(MyCustonMetric pMetricType, Request<?> pReq, Object pResp) {
	        IMetrics m = pReq.getMetrics();
	        TimingInfo ti = m.getTimingInfo();
	        List<Object> oValues = m.getProperty(pMetricType);
	        if (oValues == null || oValues.isEmpty() || oValues.get(0) == null) {
	            return Collections.emptyList();
	        }
	        return Collections.singletonList(new MetricDatum()
	                .withMetricName(pReq.getServiceName())
	                .withDimensions(new Dimension()
	                    .withName(Dimensions.MetricType.name())
	                    .withValue(pMetricType.name()))
	                .withUnit(StandardUnit.Count)
	                .withValue(Double.valueOf( oValues.get(0).toString() ))
	                .withTimestamp(endTimestamp(ti)));
	    }
		
	}
	
	private static class Client extends SimpleClient {

		public Client(APIConfiguration pConfiguration, HttpClient pHttpClient, int pPort) {
			super(pConfiguration, pHttpClient, pPort);
		}
		
		public String getIt2() {
			MyRequest oMyRequest = new MyRequest();
			ExecutionContext oContext = createExecutionContext(oMyRequest);
			IMetrics oMetrics = oContext.getMetrics();
			Request<MyRequest> oRequest = null;
			Response<String> oResponse = null;
			oMetrics.startEvent(APIRequestMetrics.ClientExecuteTime);
			try {
				oMetrics.startEvent(APIRequestMetrics.RequestMarshallTime);
				try {
					oMetrics.addProperty(MyCustonMetric.TotalResultsRequested, 10);
					oRequest = new DefaultRequest(oMyRequest, "GetIt2");
					oRequest.setHttpMethod(HttpMethodName.GET);
					oRequest.setResourcePath("/json/");
					oRequest.setMetrics(oMetrics);
				} finally {
					oMetrics.endEvent(APIRequestMetrics.RequestMarshallTime);
				}
				Response<String> oActualResponse = invoke(oRequest, null, oContext);
				return oActualResponse.getAPIResponse();
			} catch (APIServiceException e) {
				throw e;
			} catch (Exception e) {
				throw new APIClientException(e);
			} finally {
				endClientExecution(oMetrics, oRequest, oResponse);
			}
		}
		
	}
	
	@Test(timeout=60000)
	public void doIt() throws Exception {
		HttpClient oHttpClient = HttpClients.createDefault();
		APIConfiguration oAPIConfig = new APIConfiguration();
		oAPIConfig.setProfilingEnabled(true);
		Client oClient = new Client(oAPIConfig, oHttpClient, getPort());
		
		Config oConfig = new Config();
		CloudWatchConfig oCWConfig = new CloudWatchConfig();
		oConfig.setCloudWatchConfig(oCWConfig);
		
		oCWConfig.setCloudWatchEndPoint("http://localhost:" + getCloudWatchPort());
		oCWConfig.setQueuePollTimeoutMilli(TimeUnit.SECONDS.toMillis(5));
		oCWConfig.setCredentialsProvider(new StaticCredentialsProvider(new BasicAWSCredentials("", "")));
		
		MetricsConfig oMConfig = new MetricsConfig();
		oConfig.setMetricsConfig(oMConfig);
		oMConfig.setMetricNameSpace("Antiope/Test2");
		// Reset default metrics to just Client Execute Time
		oMConfig.getMetricsRegistry().setMetricTypes(Arrays.asList( APIRequestMetrics.ClientExecuteTime, MyCustonMetric.TotalResultsRequested));
		
		oMConfig.setMetricTransformer(
				new CompositeMetricTransformer()
				//.with( new PredefinedMetricTransformer() )
				.with( new CustomMetricTransformer() ));
		//oMConfig.getMetricsRegistry().setMetricTypes(new ArrayList<MetricType>());
		oMConfig.setMachineMetricExcluded(true);
		IMetricsCollector oMetricsCollector = new DefaultMetricsCollectorFactory(oConfig).getInstance();
		oClient.setMetricsCollector(oMetricsCollector);
		
		for (int i = 0; i < 20; i++) {
			Thread.sleep(1000);
			oClient.getIt2();
		}
	}
}
