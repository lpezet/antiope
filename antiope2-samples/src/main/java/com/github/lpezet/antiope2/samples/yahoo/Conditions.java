/**
 * 
 */
package com.github.lpezet.antiope2.samples.yahoo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author luc
 *
 */
public class Conditions {

	@JsonProperty("temp")
	private int mTemperature;
	private String mText;
	private int mCode;
	private String mDate;
	
	public int getTemperature() {
		return mTemperature;
	}
	public void setTemperature(int pTemperature) {
		mTemperature = pTemperature;
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
	public String getDate() {
		return mDate;
	}
	public void setDate(String pDate) {
		mDate = pDate;
	}
}
