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

/**
 * @author luc
 *
 */
public class CRC32MismatchException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new CRC32MismatchException with the specified message, and root
     * cause.
     *
     * @param message
     *            An error message describing why this exception was thrown.
     * @param t
     *            The underlying cause of this exception.
     */
    public CRC32MismatchException(String message, Throwable t) {
        super(message, t);
    }

    /**
     * Creates a new CRC32MismatchException with the specified message.
     *
     * @param message
     *            An error message describing why this exception was thrown.
     */
    public CRC32MismatchException(String message) {
        super(message);
    }

}
