/**
 * 
 */
package com.github.lpezet.antiope2.dao.http;

import java.util.List;
import java.util.Map;

/**
 * @author Luc Pezet
 *
 */
public abstract class HttpBase implements IHttpBase {

	/** Map of the headers included in this request */
    protected Headers mHeaders = new Headers();

    @Override
    public void addHeader(String name, String value) {
        mHeaders.add(name, value);
    }

    @Override
    public List<Header> getHeaders() {
        return mHeaders.getAllHeaders();
    }
    
    @Override
    public String getFirstHeaderValue(String pName) {
    	return mHeaders.getFirstValue(pName);
    }
    
    @Override
    public String getLastHeaderValue(String pName) {
    	return mHeaders.getLastValue(pName);
    }
    
    public void setHeaders(Map<String, String> headers) {
    	mHeaders.setAll(headers);
    }
}
