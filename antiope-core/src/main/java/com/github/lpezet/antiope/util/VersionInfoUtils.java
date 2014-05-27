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
package com.github.lpezet.antiope.util;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lpezet.antiope.be.VersionInfo;

/**
 * Utility class for accessing API SDK versioning information.
 */
public class VersionInfoUtils {
    
    /** Shared logger for any issues while loading version information */
    private static Logger mLogger = LoggerFactory.getLogger(VersionInfoUtils.class);

    
    /**
     * Loads the versionInfo.properties file and return instance of VersionInfoUtils.
     * E.g.: /com/amazonaws/sdk/versionInfo.properties
     */
    public static VersionInfo load(String pVersionInfoFile) {
    	InputStream inputStream = ClassLoaderHelper.getResourceAsStream(pVersionInfoFile, true, VersionInfoUtils.class);
    	
    	String oAPIName = "unknown-api";
    	String oVersion = "unknown-version";
    	String oPlatform = "java";
        Properties versionInfoProperties = new Properties();
        try {
            if (inputStream == null)
                throw new Exception(pVersionInfoFile + " not found on classpath");
            versionInfoProperties.load(inputStream);
            oAPIName = versionInfoProperties.getProperty("api");
            oVersion = versionInfoProperties.getProperty("version");
            oPlatform = versionInfoProperties.getProperty("platform");
        } catch (Exception e) {
            mLogger.info("Unable to load version information for the running API: " + e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {}
        }
        String oUserAgent = initializeUserAgent(oAPIName, oPlatform, oVersion);
        return new VersionInfo(oAPIName, oVersion, oPlatform, oUserAgent);
    }
			
    /**
     * Loads the versionInfo.properties file from the API Java SDK and
     * stores the information so that the file doesn't have to be read the
     * next time the data is needed.
     */
    private static String initializeUserAgent(String pAPIName, String pPlatform, String pVersion) {
        StringBuilder buffer = new StringBuilder(1024);

        buffer.append(pAPIName);
        buffer.append("-");
        buffer.append(pPlatform.toLowerCase());
        buffer.append("/");

        buffer.append(pVersion);
        buffer.append(" ");
        buffer.append(replaceSpaces(System.getProperty("os.name")));
        buffer.append("/");
        buffer.append(replaceSpaces(System.getProperty("os.version")));

        buffer.append(" ");
        buffer.append(replaceSpaces(System.getProperty("java.vm.name")));
        buffer.append("/");
        buffer.append(replaceSpaces(System.getProperty("java.vm.version")));

        String language = System.getProperty("user.language");
        String region = System.getProperty("user.region");

        if (language != null && region != null) {
            buffer.append(" ");
            buffer.append(replaceSpaces(language));
            buffer.append("_");
            buffer.append(replaceSpaces(region));
        }

        return buffer.toString();
    }

    /**
     * Replace any spaces in the input with underscores.
     *
     * @param input the input
     * @return the input with spaces replaced by underscores
     */
    private static String replaceSpaces(final String input) {
        return input.replace(' ', '_');
    }
}
