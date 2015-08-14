/**
 * 
 */
package com.github.lpezet.antiope2.samples.yahoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lpezet.antiope2.retrofitted.AntiopeError;
import com.github.lpezet.antiope2.retrofitted.ErrorHandler;
import com.github.lpezet.antiope2.retrofitted.RestAdapter;
import com.github.lpezet.antiope2.retrofitted.converter.JacksonConverter;

/**
 * @author Luc Pezet
 *
 */
public class YahooWeatherAPITest {
	
	private static Logger mLogger = LoggerFactory.getLogger(YahooWeatherAPITest.class);
	
	private static class MyErrorHandler implements ErrorHandler {
		@Override
		public Throwable handleError(AntiopeError pCause) {
			mLogger.error("Error!", pCause);
			return pCause;
		}
	}
	
	private static final ErrorHandler ERROR_HANDLER = new MyErrorHandler();
	
	@Test
	public void forecastRetrofitted() throws Exception {
		RestAdapter oAdapter = new RestAdapter.Builder()
				.endpoint("http://query.yahooapis.com/")
				.converter(new JacksonConverter())
				.errorHandler(ERROR_HANDLER)
				.build();
		YahooWeatherAPI oAPI = oAdapter.create(YahooWeatherAPI.class);
		
		ForecastResponse oResponse = oAPI.forecast("48907");
		assertThat( oResponse ).isNotNull();
		assertNotNull( oResponse.getQuery() );
		assertEquals(1, oResponse.getQuery().getCount());
		assertNotNull( oResponse.getQuery().getResults() );
		assertNotNull( oResponse.getQuery().getResults().getChannel() );
		assertNotNull( oResponse.getQuery().getResults().getChannel().getItem() );
		//printJson( oResponse );
	}

	private void printJson(Object pValue) throws Exception {
		System.out.println( new ObjectMapper().writeValueAsString( pValue ) );
	}
}
