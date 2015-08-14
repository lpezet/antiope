/**
 * 
 */
package com.github.lpezet.antiope2.samples.yahoo;


/**
 * @author Luc Pezet
 *
 */
public class Item {
	
	private String mTitle;
	private double mLat;
	private double mLong;
	private String mLink;
	
	//TODO: For some reason it doesn't work. Write simple unit test with Jackson and Date/Format handling, to see what's going on.
	//@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="EEE, dd MMM yyyy K:mm a zzz'Z'")
	private String mPubDate;
	private String mDescription;
	
	private Conditions mCondition;
	private Forecast[] mForecast;
	private GUID mGuid;
	public String getTitle() {
		return mTitle;
	}
	public void setTitle(String pTitle) {
		mTitle = pTitle;
	}
	public double getLat() {
		return mLat;
	}
	public void setLat(double pLat) {
		mLat = pLat;
	}
	public double getLong() {
		return mLong;
	}
	public void setLong(double pL) {
		mLong = pL;
	}
	public String getLink() {
		return mLink;
	}
	public void setLink(String pLink) {
		mLink = pLink;
	}
	public String getPubDate() {
		return mPubDate;
	}
	public void setPubDate(String pPubDate) {
		mPubDate = pPubDate;
	}
	public String getDescription() {
		return mDescription;
	}
	public void setDescription(String pDescription) {
		mDescription = pDescription;
	}
	public Conditions getCondition() {
		return mCondition;
	}
	public void setCondition(Conditions pCondition) {
		mCondition = pCondition;
	}
	public Forecast[] getForecast() {
		return mForecast;
	}
	public void setForecast(Forecast[] pForecast) {
		mForecast = pForecast;
	}
	public GUID getGuid() {
		return mGuid;
	}
	public void setGuid(GUID pGuid) {
		mGuid = pGuid;
	}

}
