package com.github.lpezet.antiope.metrics.aws;

import org.apache.http.client.HttpClient;

import com.github.lpezet.antiope.APIClientException;
import com.github.lpezet.antiope.APIServiceException;
import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.be.BasicAPICredentials;
import com.github.lpezet.antiope.bo.AdvancedAPIClient;
import com.github.lpezet.antiope.dao.DefaultHttpRequestFactory;
import com.github.lpezet.antiope.dao.DefaultRequest;
import com.github.lpezet.antiope.dao.ExecutionContext;
import com.github.lpezet.antiope.dao.HttpMethodName;
import com.github.lpezet.antiope.dao.HttpResponseHandler;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.metrics.APIRequestMetrics;
import com.github.lpezet.antiope.metrics.IMetrics;
import com.github.lpezet.antiope.metrics.aws.Sample.MyRequest;
import com.github.lpezet.antiope.transform.Unmarshaller;

class SimpleClient extends AdvancedAPIClient {
	public SimpleClient(APIConfiguration pConfiguration, HttpClient pHttpClient, int pPort) {
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
				oRequest = new DefaultRequest(oMyRequest, "GetIt");
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