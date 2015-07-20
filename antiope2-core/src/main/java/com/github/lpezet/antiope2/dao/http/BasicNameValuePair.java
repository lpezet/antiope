/**
 * 
 */
package com.github.lpezet.antiope2.dao.http;

import java.util.Objects;

/**
 * @author Luc Pezet
 *
 */
public class BasicNameValuePair implements NameValuePair {

	private String mName;
	private String mValue;
	
	public BasicNameValuePair(String pName, String pValue) {
		mName = pName;
		mValue = pValue;
	}
	
	@Override
	public String getName() {
		return mName;
	}
	@Override
	public String getValue() {
		return mValue;
	}
	
	@Override
	public boolean equals(Object pObj) {
		if (!NameValuePair.class.isInstance( pObj )) return false;
		NameValuePair oNVP = (NameValuePair) pObj;
		if (!mName.equals( oNVP.getName() )) return false;
		return
				(mValue == null && oNVP.getValue() == null) 
				|| mValue != null && mValue.equals( oNVP.getValue() );
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(mName, mValue);
	}
	
	@Override
	public String toString() {
		return mName + "=" + mValue;
	}
}
