/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import static org.junit.Assert.assertNotNull;

import org.apache.http.client.HttpClient;
import org.junit.Test;

import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.be.BasicAPICredentials;
import com.github.lpezet.antiope.be.VersionInfo;
import com.github.lpezet.antiope.dao.DefaultHttpClientFactory;
import com.github.lpezet.antiope.metrics.LogMetricsCollector;
import com.github.lpezet.antiope.samples.yahoo.WeatherRequest.TemperatureUnit;
import com.github.lpezet.antiope.util.VersionInfoUtils;

/**
 * @author luc
 *
 */
public class YahooClientTest {
	
	@Test
	public void versionInfo() throws Exception {
		VersionInfo oVI = VersionInfoUtils.load("/META-INF/yahoo_version.properties");
		assertNotNull(oVI);
		System.out.println(String.format("API: %s, Version: %s, Platform: %s, User Agent: %s", 
				oVI.getAPIName(), oVI.getVersion(), oVI.getPlatform(), oVI.getUserAgent()));
	}

	@Test
	public void simpleClient() throws Exception {
		APIConfiguration oConfig = new APIConfiguration();
		oConfig.setProfilingEnabled(true);
		HttpClient oHttpClient = new DefaultHttpClientFactory().createHttpClient(oConfig);
		YahooBaseClient oMyClient = new YahooBaseClient(oConfig, new BasicAPICredentials("abc", "abc"), oHttpClient);
		//oMyClient.setMetricsCollector(new LogMetricsCollector());
		
		doIt(oMyClient);
	}
	
	@Test
	public void advancedClient() throws Exception {
		APIConfiguration oConfig = new APIConfiguration();
		oConfig.setProfilingEnabled(true);
		HttpClient oHttpClient = new DefaultHttpClientFactory().createHttpClient(oConfig);
		YahooAdvancedClient oMyClient = new YahooAdvancedClient(oConfig, new BasicAPICredentials("abc", "abc"), oHttpClient);
		oMyClient.setMetricsCollector(new LogMetricsCollector());
		doIt(oMyClient);
	}
	
	private void doIt(IYahooClient pClient) throws Exception {
		
		WeatherRequest oRequest = new WeatherRequest().withWOEID("2502265").withTemperatureUnit(TemperatureUnit.Celsius);
		//oRequest.setMetricsCollector(new LogMetricsCollector());
		
		WeatherResponse oResponse = pClient.getWeather(oRequest);
		assertNotNull(oResponse);
		Weather oActual = oResponse.getResult();
		assertNotNull(oActual);
		System.out.println(String.format(
				"City: %s, Region: %s, Country: %s\nWind chill: %s, direction: %s, speed: %s\n" +
				"Today: %s, Temperature: %s, %s\n" +
				"Humidity: %s, Visibility: %s, Pressure: %s, Rising: %s\n" +
				"Sunrise: %s, Sunset: %s",
				oActual.getLocation().getCity(), oActual.getLocation().getRegion(), oActual.getLocation().getCountry(),
				oActual.getWind().getChill() + oActual.getUnits().getTemperature(), oActual.getWind().getDirection(), oActual.getWind().getSpeed() + oActual.getUnits().getSpeed(),
				oActual.getCurrentConditions().getDate(), oActual.getCurrentConditions().getTemperature() + oActual.getUnits().getTemperature(), oActual.getCurrentConditions().getText(),
				oActual.getAtmosphere().getHumidity() + "%", oActual.getAtmosphere().getVisibility() + oActual.getUnits().getDistance(), oActual.getAtmosphere().getPressure() + oActual.getUnits().getPressure(), oActual.getAtmosphere().getRising(),
				oActual.getAstronomy().getSunrise(), oActual.getAstronomy().getSunset()));
		System.out.println( oActual.getForecasts().size() + " days forecast:");
		for (Forecast f : oActual.getForecasts()) {
			System.out.println(String.format(
					"Day: %s, low: %s, high: %s, %s",
					f.getDay(), f.getLow() + oActual.getUnits().getTemperature(), f.getHigh() + oActual.getUnits().getTemperature(), f.getText()
					));
		}
		
		assertNotNull(oActual.getLocation());
		assertNotNull(oActual.getLocation().getCity());
		assertNotNull(oActual.getLocation().getRegion());
		assertNotNull(oActual.getLocation().getCountry());
		assertNotNull(oActual.getAstronomy());
		assertNotNull(oActual.getAstronomy().getSunrise());
		assertNotNull(oActual.getAstronomy().getSunset());
	}
}
