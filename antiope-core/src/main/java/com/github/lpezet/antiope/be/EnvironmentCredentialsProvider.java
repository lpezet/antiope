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

import com.github.lpezet.antiope.APIClientException;

/**
 * @author luc
 *
 */
public class EnvironmentCredentialsProvider implements IAPICredentialsProvider {

	private APIConfiguration mAPIConfiguration;
	
	public EnvironmentCredentialsProvider(APIConfiguration pConfiguration) {
		mAPIConfiguration = pConfiguration;
	}

	public IAPICredentials getCredentials() {
        String accessKey = System.getenv(mAPIConfiguration.getEnvVarAccessKey());
        //if (accessKey == null) {
        //    accessKey = System.getenv(SDKGlobalConfiguration.ALTERNATE_ACCESS_KEY_ENV_VAR);
        //}

        String secretKey = System.getenv(mAPIConfiguration.getEnvVarSecretKey());
        //if (secretKey == null) {
        //    secretKey = System.getenv(SDKGlobalConfiguration.ALTERNATE_SECRET_KEY_ENV_VAR);
        //}

        if (accessKey != null && secretKey != null) {
            return new BasicAPICredentials(accessKey, secretKey);
        }

        throw new APIClientException(
                "Unable to load API credentials from environment variables " +
                "(" + mAPIConfiguration.getEnvVarAccessKey() // " (or " + SDKGlobalConfiguration.ALTERNATE_ACCESS_KEY_ENV_VAR 
                + ") and " + mAPIConfiguration.getEnvVarSecretKey() + ")."); // (or " + SDKGlobalConfiguration.ALTERNATE_SECRET_KEY_ENV_VAR + "))");
    }

    public void refresh() {}

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
