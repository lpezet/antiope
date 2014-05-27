/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import javax.xml.stream.events.XMLEvent;

import com.github.lpezet.antiope.transform.StaxUnmarshallerContext;
import com.github.lpezet.antiope.transform.Unmarshaller;

/**
 * @author luc
 */
public class WeatherResponseUnmarshaller implements Unmarshaller<WeatherResponse, StaxUnmarshallerContext> {

	public WeatherResponse unmarshall(StaxUnmarshallerContext context) throws Exception {
		WeatherResponse oResponse = new WeatherResponse();
		Weather oWeather = oResponse.getResult();

		while (true) {
			XMLEvent xmlEvent = context.nextEvent();
			if (xmlEvent.isEndDocument()) return oResponse;
			if (xmlEvent.isAttribute() || xmlEvent.isStartElement()) {
				if (context.testExpression("location")) {
					oWeather.setLocation( LocationUnmarshaller.getInstance().unmarshall(context) );
				} else if (context.testExpression("wind")) {
					oWeather.setWind( WindUnmarshaller.getInstance().unmarshall(context) );
				} else if (context.testExpression("atmosphere")) {
					oWeather.setAtmosphere( AtmosphereUnmarshaller.getInstance().unmarshall(context) );
				} else if (context.testExpression("astronomy")) {
					oWeather.setAstronomy( AstronomyUnmarshaller.getInstance().unmarshall(context) );
				} else if (context.testExpression("forecast")) {
					oWeather.getForecasts().add( ForecastUnmarshaller.getInstance().unmarshall(context) );
				} else if (context.testExpression("units")) {
					oWeather.setUnits( UnitsUnmarshaller.getInstance().unmarshall(context) );
				} else if (context.testExpression("condition")) {
					oWeather.setCurrentConditions( ConditionsUnmarshaller.getInstance().unmarshall(context) );
				}
			}
		}
	}

	private static WeatherResponseUnmarshaller	instance;

	public static WeatherResponseUnmarshaller getInstance() {
		if (instance == null) instance = new WeatherResponseUnmarshaller();
		return instance;
	}

}
