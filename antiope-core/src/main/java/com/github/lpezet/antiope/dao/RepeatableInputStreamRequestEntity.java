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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author luc
 *
 */
public class RepeatableInputStreamRequestEntity  extends BasicHttpEntity {

    /** True if the request entity hasn't been written out yet */
    private boolean firstAttempt = true;

    /** The underlying InputStreamEntity being delegated to */
    private InputStreamEntity inputStreamRequestEntity;

    /** The InputStream containing the content to write out */
    private InputStream content;

    /** Shared logger for more debugging information */
    private static final Logger log = LoggerFactory.getLogger(RepeatableInputStreamRequestEntity.class);

    /**
     * Record the original exception if we do attempt a retry, so that if the
     * retry fails, we can report the original exception. Otherwise, we're most
     * likely masking the real exception with an error about not being able to
     * reset far enough back in the input stream.
     */
    private IOException originalException;


    /**
     * Creates a new RepeatableInputStreamRequestEntity using the information
     * from the specified request. If the input stream containing the request's
     * contents is repeatable, then this RequestEntity will report as being
     * repeatable.
     *
     * @param request
     *            The details of the request being written out (content type,
     *            content length, and content).
     */
    public RepeatableInputStreamRequestEntity(final Request<?> request) {
    	setChunked(false);

        /*
         * If we don't specify a content length when we instantiate our
         * InputStreamRequestEntity, then HttpClient will attempt to
         * buffer the entire stream contents into memory to determine
         * the content length.
         *
         * TODO: It'd be nice to have easier access to content length and
         *       content type from the request, instead of having to look
         *       directly into the headers.
         */
        long contentLength = -1;
        try {
            String contentLengthString = request.getHeaders().get("Content-Length");
            if (contentLengthString != null) {
                contentLength = Long.parseLong(contentLengthString);
            }
        } catch (NumberFormatException nfe) {
            log.warn("Unable to parse content length from request.  " +
            		"Buffering contents in memory.");
        }

        String contentType = request.getHeaders().get("Content-Type");
        /*
        ThroughputMetricType type = ServiceMetricTypeGuesser
                .guessThroughputMetricType(request,
                        ServiceMetricType.UPLOAD_THROUGHPUT_NAME_SUFFIX,
                        ServiceMetricType.UPLOAD_BYTE_COUNT_NAME_SUFFIX);
		*/
        //if (type == null) {
            inputStreamRequestEntity = 
                new InputStreamEntity(request.getContent(), contentLength);
        //} else {
        //    inputStreamRequestEntity = 
        //        new MetricInputStreamEntity(type, request.getContent(), contentLength);
        //}
        inputStreamRequestEntity.setContentType(contentType);
        content = request.getContent();

        setContent(content);
        setContentType(contentType);
        setContentLength(contentLength);
    }

    @Override
	public boolean isChunked() {
    	return false;
	}

    /**
     * Returns true if the underlying InputStream supports marking/reseting or
     * if the underlying InputStreamRequestEntity is repeatable.
     *
     * @see org.apache.http.HttpEntity#isRepeatable()
     */
    @Override
    public boolean isRepeatable() {
        return content.markSupported() || inputStreamRequestEntity.isRepeatable();
    }

    /**
     * Resets the underlying InputStream if this isn't the first attempt to
     * write out the request, otherwise simply delegates to
     * InputStreamRequestEntity to write out the data.
     * <p>
     * If an error is encountered the first time we try to write the request
     * entity, we remember the original exception, and report that as the root
     * cause if we continue to encounter errors, rather than masking the
     * original error.
     *
     * @see org.apache.http.HttpEntity#writeTo(java.io.OutputStream)
     */
    @Override
    public void writeTo(OutputStream output) throws IOException {
        try {
            if (!firstAttempt && isRepeatable()) content.reset();

            firstAttempt = false;
            inputStreamRequestEntity.writeTo(output);
        } catch (IOException ioe) {
            if (originalException == null) originalException = ioe;
            throw originalException;
        }
    }

}
