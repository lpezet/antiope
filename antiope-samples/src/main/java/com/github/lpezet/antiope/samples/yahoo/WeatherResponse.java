/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import com.github.lpezet.antiope.be.APIWebServiceResponse;

/**
 * @author luc
 *
 */
public class WeatherResponse extends APIWebServiceResponse<Weather> {
	
	public WeatherResponse() {
		setResult(new Weather());
	}
}
