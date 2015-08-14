/**
 * 
 */
package com.github.lpezet.antiope2.samples.yahoo;


/**
 * @author luc
 *
 */
public class Forecast {

	private String mDay;
	
	//JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd MMM yyyy")
	private String mDate;
	
	private int mLow;
	private int mHigh;
	private String mText;
	private int mCode;
	public String getDay() {
		return mDay;
	}
	public void setDay(String pDay) {
		mDay = pDay;
	}
	public String getDate() {
		return mDate;
	}
	public void setDate(String pDate) {
		mDate = pDate;
	}
	public int getLow() {
		return mLow;
	}
	public void setLow(int pLow) {
		mLow = pLow;
	}
	public int getHigh() {
		return mHigh;
	}
	public void setHigh(int pHigh) {
		mHigh = pHigh;
	}
	public String getText() {
		return mText;
	}
	public void setText(String pText) {
		mText = pText;
	}
	public int getCode() {
		return mCode;
	}
	public void setCode(int pCode) {
		mCode = pCode;
	}
	
}
