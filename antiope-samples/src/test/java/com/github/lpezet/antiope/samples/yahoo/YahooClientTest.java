/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lpezet.antiope.APIServiceException;
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
	
	private Logger mLogger = LoggerFactory.getLogger(YahooClientTest.class);
	
	@Test
	public void simulator() throws Exception {
		APIConfiguration oConfig = new APIConfiguration();
		oConfig.setProfilingEnabled(true);
		HttpClient oMockedHttpClient = mock(HttpClient.class);
		
		final HttpResponse o500Response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR, "ISE");
		final HttpResponse o503Response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_SERVICE_UNAVAILABLE, "Temporary overloaded.");
		final HttpResponse o200Response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null);
		StringEntity o200Entity = new StringEntity( IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("yahoo_weather_sample.rss")) );
		o200Response.setEntity(o200Entity);
		Answer<HttpResponse> oDefaultAnswer = new Answer<HttpResponse>() {
			@Override
			public HttpResponse answer(InvocationOnMock pInvocation) throws Throwable {
				mLogger.info("######### Static OK response...");
				return o200Response;
			}
		};
		
		ProbabilisticAnswers<HttpResponse> oAnswers = new ProbabilisticAnswers<HttpResponse>(512, oDefaultAnswer)
				.answerWith(new Answer<HttpResponse>() {
					@Override
					public HttpResponse answer(InvocationOnMock pInvocation) throws Throwable {
						mLogger.info("######### Returning 500 HTTP response.");
						return o500Response;
					}
				}, 0.10)
				.answerWith(new Answer<HttpResponse>() {
					@Override
					public HttpResponse answer(InvocationOnMock pInvocation) throws Throwable {
						mLogger.info("######### Returning 503 HTTP response.");
						return o503Response;
					}
				}, 0.25)
				.answerWith(new Answer<HttpResponse>() {
					@Override
					public HttpResponse answer(InvocationOnMock pInvocation) throws Throwable {
						mLogger.info("######### Simulating very long response...");
						Thread.sleep(5000); // 5s
						return o200Response;
					}
				}, 0.05);
		when(oMockedHttpClient.execute(isA(HttpUriRequest.class))).thenAnswer(oAnswers);
		
		YahooBaseClient oMyClient = new YahooBaseClient(oConfig, new BasicAPICredentials("abc", "abc"), oMockedHttpClient);
		//oMyClient.setMetricsCollector(new LogMetricsCollector());
		
		WeatherRequest oRequest = new WeatherRequest().withWOEID("2502265").withTemperatureUnit(TemperatureUnit.Celsius);
		//oRequest.setMetricsCollector(new LogMetricsCollector());
		
		for (int i = 0; i < 512; i++) {
			try {
				WeatherResponse oResponse = oMyClient.getWeather(oRequest);
				assertNotNull(oResponse);
			} catch (APIServiceException e) {
				Answer<HttpResponse> oCurrentAnswer = oAnswers.getCurrentAnswer();
				//TODO: Normal if got an HTTP Error
			}
			// Validate behavior:
			// 1. If 500 error --> expect....
			// 2. If 503 error --> expect....
			// 3. If too slow --> expect....
			// 4. If 200 response --> expect...
		}
	}
	
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
		
		WeatherResponse oResponse = doIt(oMyClient);
		printIt(oResponse);
	}
	
	@Test
	public void advancedClient() throws Exception {
		APIConfiguration oConfig = new APIConfiguration();
		oConfig.setProfilingEnabled(true);
		HttpClient oHttpClient = new DefaultHttpClientFactory().createHttpClient(oConfig);
		YahooAdvancedClient oMyClient = new YahooAdvancedClient(oConfig, new BasicAPICredentials("abc", "abc"), oHttpClient);
		oMyClient.setMetricsCollector(new LogMetricsCollector());
		WeatherResponse oResponse = doIt(oMyClient);
		printIt(oResponse);
	}
	
	private WeatherResponse doIt(IYahooClient pClient) throws Exception {
		
		WeatherRequest oRequest = new WeatherRequest().withWOEID("2502265").withTemperatureUnit(TemperatureUnit.Celsius);
		//oRequest.setMetricsCollector(new LogMetricsCollector());
		
		WeatherResponse oResponse = pClient.getWeather(oRequest);
		assertNotNull(oResponse);
		Weather oActual = oResponse.getResult();
		assertNotNull(oActual);
		assertNotNull(oActual.getLocation());
		assertNotNull(oActual.getLocation().getCity());
		assertNotNull(oActual.getLocation().getRegion());
		assertNotNull(oActual.getLocation().getCountry());
		assertNotNull(oActual.getAstronomy());
		assertNotNull(oActual.getAstronomy().getSunrise());
		assertNotNull(oActual.getAstronomy().getSunset());
		
		return oResponse;
	}
	
	private void printIt(WeatherResponse pResponse) {
		Weather oActual = pResponse.getResult();
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
	}
}
