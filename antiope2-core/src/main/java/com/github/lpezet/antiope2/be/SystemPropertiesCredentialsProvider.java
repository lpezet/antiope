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

import com.github.lpezet.antiope2.APIClientException;


/**
 * @author luc
 *
 */
public class SystemPropertiesCredentialsProvider implements IAPICredentialsProvider {

	private APIConfiguration mAPIConfiguration;
	
	public SystemPropertiesCredentialsProvider(APIConfiguration pConfiguration) {
		mAPIConfiguration = pConfiguration;
	}
	
	public IAPICredentials getCredentials() {
        if (System.getProperty(mAPIConfiguration.getSysPropAccessKey()) != null &&
            System.getProperty(mAPIConfiguration.getSysPropSecretKey()) != null) {
            return new BasicAPICredentials(
                    System.getProperty(mAPIConfiguration.getSysPropAccessKey()),
                    System.getProperty(mAPIConfiguration.getSysPropSecretKey()));
        }

        throw new APIClientException(
                "Unable to load API credentials from Java system properties " +
                "(" + mAPIConfiguration.getSysPropAccessKey() + " and " + mAPIConfiguration.getSysPropSecretKey() + ")");
    }

    public void refresh() {}

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
