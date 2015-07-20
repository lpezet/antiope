package com.github.lpezet.antiope2.dao.http;

import java.util.Objects;

/**
 * @author Luc Pezet
 *
 */
public class BasicHeader implements Header {

	private String mName;
	private String mValue;
	public BasicHeader(String pName, String pValue) {
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
		if (!Header.class.isInstance( pObj )) return false;
		Header h = (Header) pObj;
		return mName.equalsIgnoreCase( h.getName() ) && mValue.equals( h.getValue() );
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(mName, mValue);
	}
	
	@Override
	public String toString() {
		return mName + ": " + mValue;
	}
}
