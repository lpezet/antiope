/**
 * 
 */
package com.github.lpezet.antiope2.samples.yahoo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Luc Pezet
 *
 */
public class Query {

	private int mCount;
	private String mLang;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss'Z'")
	private Date mCreated;
	private QueryResults mResults;
	
	public int getCount() {
		return mCount;
	}
	public void setCount(int pCount) {
		mCount = pCount;
	}
	public String getLang() {
		return mLang;
	}
	public void setLang(String pLang) {
		mLang = pLang;
	}
	public Date getCreated() {
		return mCreated;
	}
	public void setCreated(Date pCreated) {
		mCreated = pCreated;
	}
	public QueryResults getResults() {
		return mResults;
	}
	public void setResults(QueryResults pResults) {
		mResults = pResults;
	}
	
}
