/**
 * The MIT License
 * Copyright (c) 2014 Luc Pezet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.lpezet.antiope2.dao.http;

import java.util.Collections;
import java.util.List;

import com.github.lpezet.antiope2.dao.ExecutionContext;
import com.github.lpezet.antiope2.metrics.IMetrics;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;
import com.github.lpezet.antiope2.metrics.StubMetrics;
import com.github.lpezet.antiope2.metrics.StubMetricsCollector;

/**
 * @author Luc Pezet
 *
 */
public class HttpExecutionContext extends ExecutionContext {

	private static final List<RequestHandler> NO_REQUEST_HANDLERS = Collections.emptyList();
	private static final IMetrics NO_METRICS = new StubMetrics();
	private static final IMetricsCollector NO_METRICS_COLLECTOR = new StubMetricsCollector();
	
	private Signer mSigner;
	private final List<RequestHandler> mRequestHandlers;
	
	public HttpExecutionContext() {
		this(NO_REQUEST_HANDLERS, NO_METRICS, NO_METRICS_COLLECTOR);
	}

	public HttpExecutionContext(IMetrics pMetrics) {
		this(NO_REQUEST_HANDLERS, pMetrics, NO_METRICS_COLLECTOR);
	}
	
	public HttpExecutionContext(IMetrics pMetrics, IMetricsCollector pMetricsCollector) {
		this(NO_REQUEST_HANDLERS, pMetrics, pMetricsCollector);
	}
	
	public HttpExecutionContext(List<RequestHandler> pRequestHandlers, IMetrics pMetrics, IMetricsCollector pMetricsCollector) {
		super(pMetrics, pMetricsCollector);
		mRequestHandlers = Collections.unmodifiableList(pRequestHandlers);
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
