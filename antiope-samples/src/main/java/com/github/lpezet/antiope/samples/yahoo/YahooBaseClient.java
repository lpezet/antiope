/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import com.github.lpezet.antiope.APIClientException;
import com.github.lpezet.antiope.APIServiceException;
import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.be.IAPICredentials;
import com.github.lpezet.antiope.bo.BaseAPIClient;
import com.github.lpezet.antiope.dao.ExecutionContext;
import com.github.lpezet.antiope.dao.HttpResponse;
import com.github.lpezet.antiope.dao.HttpResponseHandler;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.dao.StaxResponseHandler;
import com.github.lpezet.antiope.metrics.APIRequestMetrics;
import com.github.lpezet.antiope.metrics.IMetrics;
import com.github.lpezet.antiope.transform.StaxUnmarshallerContext;
import com.github.lpezet.antiope.transform.Unmarshaller;

/**
 * @author luc
 *
 */
public class YahooBaseClient extends BaseAPIClient<StaxUnmarshallerContext> implements IYahooClient {
	
	private HttpClient mHttpClient;

	public YahooBaseClient(APIConfiguration pConfiguration, IAPICredentials pCredentials, HttpClient pHttpClient) {
		super(pConfiguration, pCredentials);
		mHttpClient = pHttpClient;
		setEndpoint("http://weather.yahooapis.com/");
	}
	
	public WeatherResponse getWeather(WeatherRequest pRequest) {
		ExecutionContext oContext = createExecutionContext(pRequest);
		IMetrics oMetrics = oContext.getMetrics();
		Request<WeatherRequest> request = null;
		Response<WeatherResponse> response = null;
		oMetrics.startEvent(APIRequestMetrics.ClientExecuteTime);
		try {
			oMetrics.startEvent(APIRequestMetrics.RequestMarshallTime);
			try {
				request = new WeatherRequestMarshaller().marshall(pRequest);
				request.setMetrics(oMetrics);
			} finally {
				oMetrics.endEvent(APIRequestMetrics.RequestMarshallTime);
			}
			response = invoke(request, new WeatherResponseUnmarshaller(), oContext);
			return response.getTSGResponse();
		} catch (Exception e) {
			throw new APIClientException(e);
		} finally {
			endClientExecution(oMetrics, request, response);
		}
		
	}
	
	@Override
	protected <T> Response<T> doInvoke(Request<?> pRequest, Unmarshaller<T, StaxUnmarshallerContext> pUnmarshaller, HttpResponseHandler<APIServiceException> pErrorResponseHandler, ExecutionContext pExecutionContext) throws APIClientException, APIServiceException {
		StaxResponseHandler<T> responseHandler = new StaxResponseHandler<T>(pUnmarshaller);
		HttpGet oGet = new HttpGet(pRequest.getEndpoint() + pRequest.getResourcePath() + "?w=" + pRequest.getParameters().get("w") + "&u=" + pRequest.getParameters().get("u"));
		try {
			org.apache.http.HttpResponse oApacheHttpResponse = mHttpClient.execute(oGet);
			HttpResponse oResponse = createResponse(oGet, pRequest, oApacheHttpResponse);
			pRequest.getMetrics().startEvent(APIRequestMetrics.ResponseProcessingTime);
			T oResult = null;
			try {
				oResult = responseHandler.handle(oResponse).getResult();
			} finally {
				pRequest.getMetrics().endEvent(APIRequestMetrics.ResponseProcessingTime);
			}
			return new Response<T>(oResult, oResponse);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new APIClientException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new APIClientException(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new APIClientException(e);
		}
	}
	
	private HttpResponse createResponse(HttpRequestBase method, Request<?> request, org.apache.http.HttpResponse apacheHttpResponse) throws IOException {
		HttpResponse httpResponse = new HttpResponse(//request, 
				method);

		if (apacheHttpResponse.getEntity() != null) {
			httpResponse.setContent(apacheHttpResponse.getEntity().getContent());
		}

		httpResponse.setStatusCode(apacheHttpResponse.getStatusLine().getStatusCode());
		httpResponse.setStatusText(apacheHttpResponse.getStatusLine().getReasonPhrase());
		for (Header header : apacheHttpResponse.getAllHeaders()) {
			httpResponse.addHeader(header.getName(), header.getValue());
		}

		return httpResponse;
	}

}
