/**
 * 
 */
package com.github.lpezet.antiope2.dao.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.lpezet.antiope2.util.Assert;

/**
 * @author Luc Pezet
 *
 */
public class Headers implements Cloneable {

	private List<Header> mHeaders = new ArrayList<Header>();
	
	public void add(String pName, String pValue) {
		Assert.notNull( pName, "name == null" );
		mHeaders.add( new BasicHeader(pName, pValue) );
	}
	
	public String getFirstValue(String pName) {
		if (pName == null) return null;
		for (Header h : mHeaders) {
			if (h.getName().equalsIgnoreCase( pName )) return h.getValue();
		}
		return null;
	}
	
	public String getLastValue(String pName) {
		for (int i = mHeaders.size(); i >= 0; i--) {
			Header h = mHeaders.get(i);
			if (h.getName().equals( pName )) return h.getValue();
		}
		return null;
	}
	
	public boolean containsHeader(String pName) {
		if (pName == null) return false;
		for (Header h : mHeaders) {
			if (h.getName().equalsIgnoreCase( pName )) return true;
		}
		return false;
	}
	
	public List<Header> getAllHeaders() {
		return Collections.unmodifiableList( mHeaders );
	}
	
	public void clear() {
		mHeaders.clear();
	}
	
	public void setAll(Map<String, String> pHeaders) {
		clear();
		for (Entry<String, String> e : pHeaders.entrySet()) {
			add(e.getKey(), e.getValue());
		}
	}
	
	public void addAll(List<Header> pHeaders) {
		mHeaders.addAll( pHeaders );
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Headers oClone = new Headers();
		oClone.mHeaders.addAll( mHeaders );
		return oClone;
	}
	
	@Override
	public String toString() {
		return mHeaders.toString();
	}
	
	
}
