/**
 * 
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
