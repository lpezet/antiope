/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import com.github.lpezet.antiope.APIClientException;
import com.github.lpezet.antiope.dao.DefaultRequest;
import com.github.lpezet.antiope.dao.HttpMethodName;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.transform.Marshaller;

/**
 * @author luc
 *
 */
public class WeatherRequestMarshaller implements Marshaller<Request<WeatherRequest>, WeatherRequest> {
	
	@Override
	public Request<WeatherRequest> marshall(WeatherRequest pIn) throws Exception {
		if (pIn == null) {
			throw new APIClientException("Invalid argument passed to marshall(...)");
		}
		Request<WeatherRequest> request = new DefaultRequest<WeatherRequest>(pIn, "Weather");
		request.setHttpMethod(HttpMethodName.GET);
		request.addParameter("w", pIn.getWOEID());
		request.addParameter("u", Character.toString(pIn.getTemperatureUnit().getValue()));
		request.setResourcePath("/forecastrss");
		return request;
	}

}
