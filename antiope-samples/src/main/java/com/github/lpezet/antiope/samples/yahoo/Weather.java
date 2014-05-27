/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luc
 *
 */
public class Weather {

	private Location mLocation;
	private Wind mWind;
	private Atmosphere mAtmosphere;
	private Astronomy mAstronomy;
	private Units mUnits;
	private Conditions mCurrentConditions;
	private List<Forecast> mForecasts = new ArrayList<Forecast>();
	
	public Location getLocation() {
		return mLocation;
	}
	public void setLocation(Location pLocation) {
		mLocation = pLocation;
	}
	public Wind getWind() {
		return mWind;
	}
	public void setWind(Wind pWind) {
		mWind = pWind;
	}
	public Atmosphere getAtmosphere() {
		return mAtmosphere;
	}
	public void setAtmosphere(Atmosphere pAtmosphere) {
		mAtmosphere = pAtmosphere;
	}
	public Astronomy getAstronomy() {
		return mAstronomy;
	}
	public void setAstronomy(Astronomy pAstronomy) {
		mAstronomy = pAstronomy;
	}
	public Units getUnits() {
		return mUnits;
	}
	public void setUnits(Units pUnits) {
		mUnits = pUnits;
	}
	public List<Forecast> getForecasts() {
		return mForecasts;
	}
	public void setForecasts(List<Forecast> pForecasts) {
		mForecasts = pForecasts;
	}
	public Conditions getCurrentConditions() {
		return mCurrentConditions;
	}
	public void setCurrentConditions(Conditions pCurrentConditions) {
		mCurrentConditions = pCurrentConditions;
	}
	
	
}
