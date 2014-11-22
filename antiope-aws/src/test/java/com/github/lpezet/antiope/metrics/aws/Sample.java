/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.github.lpezet.antiope.APIClientException;
import com.github.lpezet.antiope.APIServiceException;
import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.be.APIWebServiceRequest;
import com.github.lpezet.antiope.be.APIWebServiceResponse;
import com.github.lpezet.antiope.be.BasicAPICredentials;
import com.github.lpezet.antiope.bo.AdvancedAPIClient;
import com.github.lpezet.antiope.dao.DefaultHttpRequestFactory;
import com.github.lpezet.antiope.dao.DefaultRequest;
import com.github.lpezet.antiope.dao.ExecutionContext;
import com.github.lpezet.antiope.dao.HttpMethodName;
import com.github.lpezet.antiope.dao.HttpResponse;
import com.github.lpezet.antiope.dao.HttpResponseHandler;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.metrics.APIRequestMetrics;
import com.github.lpezet.antiope.metrics.IMetrics;
import com.github.lpezet.antiope.metrics.IMetricsCollector;
import com.github.lpezet.antiope.transform.Unmarshaller;

/**
 * @author Luc Pezet
 *
 */
public class Sample {
	
	private static final String AWS_ACCESS_KEY = "";
	private static final String AWS_SECRET_KEY = "";
	

	private static class SimpleResponseHandler implements HttpResponseHandler<APIWebServiceResponse<String>> {

		@Override
		public APIWebServiceResponse<String> handle(HttpResponse pResponse) throws Exception {
			InputStream oContent = pResponse.getContent();
			String oBody = IOUtils.toString(oContent);
			APIWebServiceResponse<String> oResponse = new APIWebServiceResponse<String>();
	    	oResponse.setResult(oBody);
	    	return oResponse;
		}

		@Override
		public boolean needsConnectionLeftOpen() {
			return false;
		}
		
	}
	
	private static class MyRequest extends APIWebServiceRequest {
		
	}
	
	private static class AdvancedAPIClientImpl extends AdvancedAPIClient {
		public AdvancedAPIClientImpl(APIConfiguration pConfiguration, HttpClient pHttpClient, int pPort) {
			super(pConfiguration, new BasicAPICredentials("", ""), pHttpClient);
			setEndpoint("http://localhost:" + pPort);
			setHttpRequestFactory(new DefaultHttpRequestFactory());
		}
		
		public String getIt() {
			MyRequest oMyRequest = new MyRequest();
			ExecutionContext oContext = createExecutionContext(oMyRequest);
			IMetrics oMetrics = oContext.getMetrics();
			Request<MyRequest> oRequest = null;
			Response<String> oResponse = null;
			oMetrics.startEvent(APIRequestMetrics.ClientExecuteTime);
			try {
				oMetrics.startEvent(APIRequestMetrics.RequestMarshallTime);
				try {
					oRequest = new DefaultRequest(oMyRequest, "FreeGeoIP");
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

		@Override
		protected HttpResponseHandler createResponseHandler(ExecutionContext pContext, Unmarshaller pUnmarshaller) {
			return new SimpleResponseHandler();
		}
		
	}
	
	private Server mServer;
	private int mPort;
	
	@Before
	public void setup() throws Exception {
		mServer = new Server(0);
		mServer.setHandler(new AbstractHandler() {
			
			@Override
			public void handle(String target, 
					org.eclipse.jetty.server.Request baseRequest,
					HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				response.setContentType("text/html;charset=utf-8");
		        response.setStatus(HttpServletResponse.SC_OK);
		        baseRequest.setHandled(true);
		        response.getWriter().println("<h1>Hello World</h1>");
			}
		});
		mServer.start();
		mPort = mServer.getConnectors()[0].getLocalPort();
	}
	
	@After
	public void tearDown() throws Exception {
		mServer.stop();
		mServer.destroy();
	}
	
	@Test(timeout=60000)
	public void doIt() throws Exception {
		HttpClient oHttpClient = HttpClients.createDefault();
		APIConfiguration oAPIConfig = new APIConfiguration();
		oAPIConfig.setProfilingEnabled(true);
		AdvancedAPIClientImpl oClient = new AdvancedAPIClientImpl(oAPIConfig, oHttpClient, mPort);
		
		CloudWatchConfig oCWConfig = new CloudWatchConfig();
		oCWConfig.setQueuePollTimeoutMilli(TimeUnit.SECONDS.toMillis(5));
		oCWConfig.setCredentialsProvider(new StaticCredentialsProvider(new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)));
		Config oConfig = new Config();
		oConfig.setCloudWatchConfig(oCWConfig);
		MetricsConfig oMConfig = new MetricsConfig();
		oMConfig.setMetricNameSpace("Antiope/Test");
		oConfig.setMetricsConfig(oMConfig);
		IMetricsCollector oMetricsCollector = new DefaultMetricsCollectorFactory(oConfig).getInstance();
		oClient.setMetricsCollector(oMetricsCollector);
		
		for (int i = 0; i < 20; i++) {
			Thread.sleep(1000);
			oClient.getIt();
		}
	}
}
