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
package com.github.lpezet.antiope2.be;

import java.util.Map;

/**
 * @author luc
 *
 */
public class ResponseMetadata {

	public static final String API_REQUEST_ID = "API_REQUEST_ID";

    protected final Map<String, String> metadata;

    /**
     * Creates a new ResponseMetadata object from a specified map of raw
     * metadata information.
     * 
     * @param metadata
     *            The raw metadata for the new ResponseMetadata object.
     */
    public ResponseMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Creates a new ResponseMetadata object from an existing ResponseMetadata
     * object.
     *
     * @param originalResponseMetadata
     *            The ResponseMetadata object from which to create the new
     *            object.
     */
    public ResponseMetadata(ResponseMetadata originalResponseMetadata) {
        this(originalResponseMetadata.metadata);
    }

    /**
     * Returns the TSG request ID contained in this response metadata object.
     * TSG request IDs can be used in the event a service call isn't working as
     * expected and you need to work with TSG support to debug an issue.
     *
     * @return The TSG request ID contained in this response metadata object.
     */
    public String getRequestId() {
        return metadata.get(API_REQUEST_ID);
    }

    @Override
    public String toString() {
        if (metadata == null) return "{}";
        return metadata.toString();
    }

}
