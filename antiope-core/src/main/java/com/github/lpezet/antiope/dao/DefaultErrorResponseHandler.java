/*
 * Copyright 2011-2013 Amazon Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lpezet.antiope.dao;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import com.github.lpezet.antiope.APIServiceException;
import com.github.lpezet.antiope.transform.Unmarshaller;
import com.github.lpezet.antiope.util.XPathUtils;

/**
 * @author luc
 *
 */
public class DefaultErrorResponseHandler  implements HttpResponseHandler<APIServiceException> {

    /**
     * The list of error response unmarshallers to try to apply to error
     * responses.
     */
    private List<Unmarshaller<APIServiceException, Node>> unmarshallerList;

    /**
     * Constructs a new DefaultErrorResponseHandler that will handle error
     * responses from Amazon services using the specified list of unmarshallers.
     * Each unmarshaller will be tried, in order, until one is found that can
     * unmarshall the error response.
     *
     * @param unmarshallerList
     *            The list of unmarshallers to try using when handling an error
     *            response.
     */
    public DefaultErrorResponseHandler(List<Unmarshaller<APIServiceException, Node>> unmarshallerList) {
        this.unmarshallerList = unmarshallerList;
    }

    public APIServiceException handle(HttpResponse errorResponse)
            throws Exception {
        Document document;
        try {
            document = XPathUtils.documentFrom(errorResponse.getContent());
        } catch (SAXParseException e) {
        	APIServiceException exception = new APIServiceException(String.format("Unable to unmarshall error response (%s)", e.getMessage()), e);
            exception.setErrorCode(String.format("%s %s", errorResponse.getStatusCode(), errorResponse.getStatusText()));
            exception.setErrorType(APIServiceException.ErrorType.Unknown);
            exception.setStatusCode(errorResponse.getStatusCode());

            return exception;
        }

        /*
         * We need to select which exception unmarshaller is the correct one to
         * use from all the possible exceptions this operation can throw.
         * Currently we rely on the unmarshallers to return null if they can't
         * unmarshall the response, but we might need something a little more
         * sophisticated in the future.
         */
        for (Unmarshaller<APIServiceException, Node> unmarshaller : unmarshallerList) {
        	APIServiceException ase = unmarshaller.unmarshall(document);
            if (ase != null) {
                ase.setStatusCode(errorResponse.getStatusCode());
                return ase;
            }
        }

        throw new APIServiceException("Unable to unmarshall error response from service");
    }

    /**
     * Since this response handler completely consumes all the data from the
     * underlying HTTP connection during the handle method, we don't need to
     * keep the HTTP connection open.
     *
     */
    public boolean needsConnectionLeftOpen() {
        return false;
    }
}
