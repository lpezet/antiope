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
package com.github.lpezet.antiope.dao;



/**
 * @author luc
 *
 */
public class Response<T> {
    private final T response;
    private final HttpResponse httpResponse;
    public Response(T response, HttpResponse httpResponse) {
        this.response = response;
        this.httpResponse = httpResponse;
    }
    public T getTSGResponse() { return response; }
    public HttpResponse getHttpResponse() { return httpResponse; }

}
