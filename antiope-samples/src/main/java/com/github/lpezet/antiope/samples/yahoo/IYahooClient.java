/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

/**
 * @author luc
 *
 */
public interface IYahooClient {

	public WeatherResponse getWeather(WeatherRequest pRequest);
	
}
