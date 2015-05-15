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

import java.net.URI;
import java.util.Map;

import com.github.lpezet.antiope2.dao.IExecutionContextAware;

/**
 * @author Luc Pezet
 *
 */
public interface IHttpRequest extends IHttpBase, IExecutionContextAware {
	
    /**
     * Sets the path to the resource being requested.
     *
     * @param path
     *            The path to the resource being requested.
     */
    public void setResourcePath(String path);

    /**
     * Returns the path to the resource being requested.
     *
     * @return The path to the resource being requested.
     */
    public String getResourcePath();

    /**
     * Adds the specified request parameter to this request.
     *
     * @param name
     *            The name of the request parameter.
     * @param value
     *            The value of the request parameter.
     */
    public void addParameter(String name, String value);

    /**
     * Adds the specified request parameter to this request, and returns the
     * updated request object.
     *
     * @param name
     *            The name of the request parameter.
     * @param value
     *            The value of the request parameter.
     *
     * @return The updated request object.
     */
    //public Request<T> withParameter(String name, String value);
    public IHttpRequest withParameter(String name, String value);
    
    /**
     * Returns a map of all parameters in this request.
     *
     * @return A map of all parameters in this request.
     */
    public Map<String, String> getParameters();
    
    /**
     * Sets all parameters, clearing any existing values.
     */
    public void setParameters(Map<String, String> parameters);       

    /**
     * Returns the service endpoint (ex: "https://ec2.amazontsg.com") to which
     * this request should be sent.
     *
     * @return The service endpoint to which this request should be sent.
     */
    public URI getEndpoint();

    /**
     * Sets the service endpoint to which this request should be sent.
     *
     * @param endpoint
     *            The service endpoint to which this request should be sent.
     */
    public void setEndpoint(URI endpoint);

	/**
	 * Returns the HTTP method (GET, POST, etc) to use when sending this
	 * request.
	 * 
	 * @return The HTTP method to use when sending this request.
	 */
    public HttpMethodName getHttpMethod();

	/**
	 * Sets the HTTP method (GET, POST, etc) to use when sending this request.
	 * 
	 * @param httpMethod
	 *            The HTTP method to use when sending this request.
	 */
    public void setHttpMethod(HttpMethodName httpMethod);

    /**
     * Returns the name of the Amazon service this request is for.
     *
     * @return The name of the Amazon service this request is for.
     */
    public String getServiceName();

    /**
     * Returns the original, user facing request object which this internal
     * request object is representing.
     *
     * @return The original, user facing request object which this request
     *         object is representing.
     */
    //public T getOriginalRequest();
    
    /**
     * Returns the optional value for time offset for this request.  This
     * will be used by the signer to adjust for potential clock skew.  
     * Value is in seconds, positive values imply the current clock is "fast",
     * negative values imply clock is slow.
     * 
     * @return The optional value for time offset (in seconds) for this request.
     */
    public int getTimeOffset();
    
    /**
     * Sets the optional value for time offset for this request.  This
     * will be used by the signer to adjust for potential clock skew.  
     * Value is in seconds, positive values imply the current clock is "fast",
     * negative values imply clock is slow.
     *
     * @param timeOffset
     *            The optional value for time offset (in seconds) for this request.
     */
    public void setTimeOffset(int timeOffset);
    
    
    /**
     * Sets the optional value for time offset for this request.  This
     * will be used by the signer to adjust for potential clock skew.  
     * Value is in seconds, positive values imply the current clock is "fast",
     * negative values imply clock is slow.
     *
     * @return The updated request object.
     */
    //public Request<T> withTimeOffset(int timeOffset);
    public IHttpRequest withTimeOffset(int timeOffset); 
    
    /**
     * Returns the request metrics.
     */
    //public APIRequestMetrics getTSGRequestMetrics();

    /**
     * Bind the request metrics to the request. Note metrics can be captured
     * before the request is created.
     * 
     * @throws IllegalStateException if the binding has already occurred
     */
    //public void setTSGRequestMetrics(APIRequestMetrics metrics);
}
