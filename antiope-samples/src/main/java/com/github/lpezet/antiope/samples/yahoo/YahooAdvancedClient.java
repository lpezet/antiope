/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import org.apache.http.client.HttpClient;

import com.github.lpezet.antiope.APIClientException;
import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.be.APIWebServiceResponse;
import com.github.lpezet.antiope.be.IAPICredentials;
import com.github.lpezet.antiope.bo.AdvancedAPIClient;
import com.github.lpezet.antiope.dao.DefaultHttpRequestFactory;
import com.github.lpezet.antiope.dao.ExecutionContext;
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
public class YahooAdvancedClient extends AdvancedAPIClient<StaxUnmarshallerContext> implements IYahooClient {
	
	public YahooAdvancedClient(APIConfiguration pConfiguration, IAPICredentials pCredentials, HttpClient pHttpClient) {
		super(pConfiguration, pCredentials, pHttpClient);
		setEndpoint("http://weather.yahooapis.com/");
		setHttpRequestFactory(new DefaultHttpRequestFactory());
	}
	
	@Override
	protected <T> HttpResponseHandler<APIWebServiceResponse<T>> createResponseHandler(Unmarshaller<T, StaxUnmarshallerContext> pUnmarshaller) {
		return new StaxResponseHandler<T>(pUnmarshaller);
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
			return response.getAPIResponse();
		} catch (Exception e) {
			throw new APIClientException(e);
		} finally {
			endClientExecution(oMetrics, request, response);
		}
		
	}
}
