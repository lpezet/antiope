/**
 * 
 */
package com.github.lpezet.antiope2.samples.yahoo;

import com.github.lpezet.antiope2.retrofitted.annotation.http.GET;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Query;

/**
 * Endpoint: http://query.yahooapis.com/
 * 
 * @author Luc Pezet
 *
 */
public interface YahooWeatherAPI {

	@GET("/v1/public/yql?format=json")
	public ForecastResponse forecast(@Query(value="q", template="select item from weather.forecast where location=\"{}\"") String pLocation);
}
