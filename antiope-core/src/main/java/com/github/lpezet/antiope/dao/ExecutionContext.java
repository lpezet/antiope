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

import com.github.lpezet.antiope.be.IAPICredentials;
import com.github.lpezet.antiope.metrics.BaseMetrics;
import com.github.lpezet.antiope.metrics.IMetrics;
import com.github.lpezet.antiope.metrics.StubMetrics;

/**
 * @author luc
 *
 */
public class ExecutionContext {

	private final IMetrics mMetrics;
	private List<RequestHandler> mRequestHandlers;
	private IAPICredentials mCredentials;
	private String contextUserAgent;
	private Signer mSigner;
	
	public ExecutionContext() {
		this(null, false);
	}
	
	public ExecutionContext(List<RequestHandler> pRequestHandlers, boolean pProfilingEnabled) {
		mMetrics = pProfilingEnabled ? new BaseMetrics() : new StubMetrics();
		mRequestHandlers = pRequestHandlers;
	}

	public IAPICredentials getCredentials() {
		return mCredentials;
	}

	public void setCredentials(IAPICredentials pCredentials) {
		mCredentials = pCredentials;
	}
	
	public IMetrics getMetrics() {
		return mMetrics;
	}
	
	public String getContextUserAgent() {
		return contextUserAgent;
	}

	public void setContextUserAgent(String pContextUserAgent) {
		contextUserAgent = pContextUserAgent;
	}
	
	public List<RequestHandler> getRequestHandlers() {
		return mRequestHandlers;
	}
	
	public Signer getSigner() {
		return mSigner;
	}
	
	public void setSigner(Signer pSigner) {
		mSigner = pSigner;
	}
	
}
