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

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lpezet.antiope.APIClientException;

/**
 * @author luc
 *
 */
public class APICredentialsProviderChain implements IAPICredentialsProvider {

	private static final Logger log = LoggerFactory.getLogger(APICredentialsProviderChain.class);

    private List<IAPICredentialsProvider> mCredentialsProviders = new LinkedList<IAPICredentialsProvider>();

    private boolean mReuseLastProvider = true;
    private IAPICredentialsProvider mLastUsedProvider;


    /**
     * Constructs a new APICredentialsProviderChain with the specified
     * credential providers. When credentials are requested from this provider,
     * it will call each of these credential providers in the same order
     * specified here until one of them returns API security credentials.
     *
     * @param pCredentialsProviders
     *            The chain of credentials providers.
     */
    public APICredentialsProviderChain(IAPICredentialsProvider... pCredentialsProviders) {
        if (pCredentialsProviders == null || pCredentialsProviders.length == 0)
            throw new IllegalArgumentException("No credential providers specified");

        for (IAPICredentialsProvider provider : pCredentialsProviders) {
            this.mCredentialsProviders.add(provider);
        }
    }

    /**
     * Returns true if this chain will reuse the last successful credentials
     * provider for future credentials requests, otherwise, false if it will
     * search through the chain each time.
     *
     * @return True if this chain will reuse the last successful credentials
     *         provider for future credentials requests.
     */
    public boolean getReuseLastProvider() {
        return mReuseLastProvider;
    }

    /**
     * Enables or disables caching of the last successful credentials provider
     * in this chain. Reusing the last successful credentials provider will
     * typically return credentials faster than searching through the chain.
     *
     * @param b
     *            Whether to enable or disable reusing the last successful
     *            credentials provider for future credentials requests instead
     *            of searching through the whole chain.
     */
    public void setReuseLastProvider(boolean b) {
        this.mReuseLastProvider = b;
    }

    public IAPICredentials getCredentials() {
        if (mReuseLastProvider && mLastUsedProvider != null) {
            return mLastUsedProvider.getCredentials();
        }

        for (IAPICredentialsProvider oProvider : mCredentialsProviders) {
            try {
            	IAPICredentials credentials = oProvider.getCredentials();

                if (credentials.getAccessKey() != null &&
                    credentials.getSecretKey() != null) {
                    log.debug("Loading credentials from " + oProvider.toString());

                    mLastUsedProvider = oProvider;
                    return credentials;
                }
            } catch (Exception e) {
                // Ignore any exceptions and move onto the next provider
                log.debug("Unable to load credentials from " + oProvider.toString() +
                          ": " + e.getMessage());
            }
        }

        throw new APIClientException("Unable to load API credentials from any provider in the chain");
    }

    public void refresh() {
        for (IAPICredentialsProvider provider : mCredentialsProviders) {
            provider.refresh();
        }
    }

}
