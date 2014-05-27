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
package com.github.lpezet.antiope.be;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author luc
 *
 */
/**
 * Simple implementation APICredentials that reads in API access keys from a
 * properties file. The API access key is expected to be in the "accessKey"
 * property and the API secret key id is expected to be in the "secretKey"
 * property.
 */
public class PropertiesAPICredentials implements IAPICredentials {

    private final String accessKey;
    private final String secretAccessKey;

    /**
     * Reads the specified file as a Java properties file and extracts the
     * API access key from the "accessKey" property and API secret access
     * key from the "secretKey" property. If the specified file doesn't
     * contain the API access keys an IOException will be thrown.
     *
     * @param file
     *            The file from which to read the API credentials
     *            properties.
     *
     * @throws FileNotFoundException
     *             If the specified file isn't found.
     * @throws IOException
     *             If any problems are encountered reading the API access
     *             keys from the specified file.
     * @throws IllegalArgumentException
     *             If the specified properties file does not contain the
     *             required keys.
     */
    public PropertiesAPICredentials(File file) throws FileNotFoundException, IOException, IllegalArgumentException {
        if (!file.exists()) {
            throw new FileNotFoundException("File doesn't exist:  "
                                            + file.getAbsolutePath());
        }

        FileInputStream stream = new FileInputStream(file);
        try {

            Properties accountProperties = new Properties();
            accountProperties.load(stream);

            if (accountProperties.getProperty("accessKey") == null ||
                accountProperties.getProperty("secretKey") == null) {
                throw new IllegalArgumentException(
                    "The specified file (" + file.getAbsolutePath()
                    + ") doesn't contain the expected properties 'accessKey' "
                    + "and 'secretKey'."
                );
            }

            accessKey = accountProperties.getProperty("accessKey");
            secretAccessKey = accountProperties.getProperty("secretKey");

        } finally {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Reads the specified input stream as a stream of Java properties file
     * content and extracts the API access key ID and secret access key from the
     * properties.
     *
     * @param inputStream
     *            The input stream containing the API credential properties.
     *
     * @throws IOException
     *             If any problems occur while reading from the input stream.
     */
    public PropertiesAPICredentials(InputStream inputStream) throws IOException {
        Properties accountProperties = new Properties();
        try {
            accountProperties.load(inputStream);
        } finally {
            try {inputStream.close();} catch (Exception e) {}
        }

        if (accountProperties.getProperty("accessKey") == null ||
            accountProperties.getProperty("secretKey") == null) {
            throw new IllegalArgumentException("The specified properties data " +
                    "doesn't contain the expected properties 'accessKey' and 'secretKey'.");
        }

        accessKey = accountProperties.getProperty("accessKey");
        secretAccessKey = accountProperties.getProperty("secretKey");
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretAccessKey;
    }

}
