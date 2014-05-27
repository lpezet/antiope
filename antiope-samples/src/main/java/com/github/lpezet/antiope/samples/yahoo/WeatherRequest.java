package com.github.lpezet.antiope.samples.yahoo;

import com.github.lpezet.antiope.be.APIWebServiceRequest;

public class WeatherRequest extends APIWebServiceRequest {
	
	enum TemperatureUnit {
		Fahrenheit('f'),
		Celsius('c');
		
		private char mValue;
		
		private TemperatureUnit(char pValue) {
			mValue = pValue;
		}
		
		public char getValue() {
			return mValue;
		}
	}

	private String mWOEID;
	private TemperatureUnit mTemperatureUnit = TemperatureUnit.Fahrenheit;
	
	public WeatherRequest withTemperatureUnit(TemperatureUnit pValue) {
		mTemperatureUnit = pValue;
		return this;
	}
	
	public WeatherRequest withWOEID(String pValue) {
		mWOEID = pValue;
		return this;
	}

	public String getWOEID() {
		return mWOEID;
	}

	public void setWOEID(String pWOEID) {
		mWOEID = pWOEID;
	}

	public TemperatureUnit getTemperatureUnit() {
		return mTemperatureUnit;
	}

	public void setTemperatureUnit(TemperatureUnit pTemperatureUnit) {
		mTemperatureUnit = pTemperatureUnit;
	}
	
	
	
	
}
