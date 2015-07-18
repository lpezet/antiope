/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * 
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.github.lpezet.antiope2.dao.http;

import java.io.InputStream;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.annotation.NotThreadSafe;

import com.github.lpezet.antiope2.dao.ExecutionContext;

/**
 * @author luc
 *
 */
@NotThreadSafe
public class HttpRequest implements IHttpRequest {

	/** The resource path being requested */
    private String resourcePath;

    /** Map of the parameters being sent as part of this request */
    private Map<String, String> parameters = new LinkedHashMap<String, String>();

    /** Map of the headers included in this request */
    private Map<String, String> headers = new LinkedHashMap<String, String>();

    /** The service endpoint to which this request should be sent */
    private URI endpoint;

    /** The name of the service to which this request is being sent */
    private String serviceName;

    /**
     * The original, user facing request object which this internal request
     * object is representing
     */
    //private final T originalRequest;

    /** The HTTP method to use when sending this request. */
    private String httpMethod = HttpMethodName.POST.name();

    /** An optional stream from which to read the request payload. */
    private InputStream content;
    
    /** An optional time offset to account for clock skew */
    private int timeOffset;
    
    private ExecutionContext mExecutionContext;
    
    /**
     * Constructs a new DefaultRequest with the specified service name and the
     * original, user facing request object.
     *
     * @param serviceName
     *            The name of the service to which this request is being sent.
     * @param originalRequest
     *            The original, user facing, API request being represented by
     *            this internal request object.
     */
    /*
    public HttpRequest(T originalRequest, String serviceName) {
        this.serviceName = serviceName;
        this.originalRequest = originalRequest;
    }
	*/
    /**
     * Constructs a new DefaultRequest with the specified service name and no
     * specified original, user facing request object.
     *
     * @param serviceName
     *            The name of the service to which this request is being sent.
     */
    public HttpRequest(String serviceName) {
    	this.serviceName = serviceName;
        //this(null, serviceName);
    }
    
    @Override
    public ExecutionContext getExecutionContext() {
    	return mExecutionContext;
    }
    
    public void setExecutionContext(ExecutionContext pExecutionContext) {
		mExecutionContext = pExecutionContext;
	}

    /**
     * Returns the original, user facing request object which this internal
     * request object is representing.
     *
     * @return The original, user facing request object which this request
     *         object is representing.
     */
    //public T getOriginalRequest() {
    //    return originalRequest;
    //}

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public HttpRequest withParameter(String name, String value) {
        addParameter(name, value);
        return this;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    public void setHttpMethod(HttpMethodName pName) {
    	this.httpMethod = pName.name();
    }

    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public String getServiceName() {
        return serviceName;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }
    
    public int getTimeOffset() {
        return timeOffset;
    }
    
    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }

    public HttpRequest withTimeOffset(int timeOffset) {
        setTimeOffset(timeOffset);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getHttpMethod()).append(" ");
        if (getEndpoint() != null) builder.append(getEndpoint()).append(" ");
        String resourcePath = getResourcePath();

        if (resourcePath == null) {
            builder.append("/");
        }
        else {
            if (!resourcePath.startsWith("/")) {
                builder.append("/");
            }
            builder.append(resourcePath);
        }
        builder.append(" ");
        if (!getParameters().isEmpty()) {
            builder.append("Parameters: (");
            for (String key : getParameters().keySet()) {
                String value = getParameters().get(key);
                builder.append(key).append(": ").append(value).append(", ");
            }
            builder.append(") ");
        }

        if (!getHeaders().isEmpty()) {
            builder.append("Headers: (");
            for (String key : getHeaders().keySet()) {
                String value = getHeaders().get(key);
                builder.append(key).append(": ").append(value).append(", ");
            }
            builder.append(") ");
        }

        return builder.toString();
    }
    
    /*
    public void setAPIRequestMetrics(APIRequestMetrics metrics) {
        if (this.metrics == null) {
            this.metrics = metrics;
        } else {
            throw new IllegalStateException("APIRequestMetrics has already been set on this request");
        }
    }
	*/
}
