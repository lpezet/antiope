/**
 * 
 */
package com.github.lpezet.antiope2.dao.http;

import java.io.InputStream;
import java.util.Map;

/**
 * @author Luc Pezet
 *
 */
public interface IHttpBase {
	/**
     * Adds the specified header to this request.
     *
     * @param name
     *            The name of the header to add.
     * @param value
     *            The header's value.
     */
    public void addHeader(String name, String value);

    /**
     * Returns a map of all the headers included in this request.
     *
     * @return A map of all the headers included in this request.
     */
    public Map<String, String> getHeaders();
    
    /**
     * Sets all headers, clearing any existing ones.
     */
    public void setHeaders(Map<String, String> headers);

	/**
	 * Returns the optional stream containing the payload data to include for
	 * this request.  Not all requests will contain payload data.
	 * 
	 * @return The optional stream containing the payload data to include for
	 *         this request.
	 */
    public InputStream getContent();

	/**
	 * Sets the optional stream containing the payload data to include for this
	 * request. Not all requests will contain payload data.
	 * 
	 * @param content
	 *            The optional stream containing the payload data to include for
	 *            this request.
	 */
    public void setContent(InputStream content);

}
