/**
 * The MIT License
 * Copyright (c) 2014 Luc Pezet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.lpezet.antiope2.dao.http;

import java.io.InputStream;
import java.util.List;
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
    public List<Header> getHeaders();
    
    public String getFirstHeaderValue(String pName);
    
    public String getLastHeaderValue(String pName);
    
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
