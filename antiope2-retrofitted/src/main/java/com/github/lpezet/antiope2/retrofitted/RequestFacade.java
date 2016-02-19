/**
 * 
 */
package com.github.lpezet.antiope2.retrofitted;

/**
 * @author Luc Pezet
 *
 */
public interface RequestFacade {

	public void addHeader(String name, String value);
	
	public void addPathParam(String pName, String pValue, boolean pUrlEncodeValue);
	
	public void addQueryParam(String name, Object value, String pValueTemplate, boolean encodeName, boolean encodeValue);
	
	public void addQueryParam(String name, Object value);
	
}
