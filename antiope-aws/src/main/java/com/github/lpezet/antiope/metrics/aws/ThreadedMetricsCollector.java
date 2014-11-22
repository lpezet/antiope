/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws;

import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.metrics.IMetricsCollector;

/**
 * @author Luc Pezet
 *
 */
public abstract class ThreadedMetricsCollector implements IMetricsCollector {
	/**
	 * Can be used to serve as a factory for the request metric collector.
	 */
	public static interface Factory {
		/**
		 * Returns an instance of the collector; or null if if failed to create
		 * one.
		 */
		public ThreadedMetricsCollector getInstance();
	}

	/**
	 * Starts the request metric collector.
	 * 
	 * @return true if the collector is successfully started; false otherwise.
	 */
	public abstract boolean start();

	/**
	 * Stops the request metric collector.
	 *
	 * @return true if the collector is successfully stopped; false if the
	 *         collector is not running and therefore the call has no effect.
	 */
	public abstract boolean stop();

	/** Returns true if this collector is enabled; false otherwise. */
	public abstract boolean isEnabled();

	/** A convenient instance of a no-op request metric collector. */
	public static final IMetricsCollector NONE = new ThreadedMetricsCollector() {
		@Override
		public boolean start() {
			return true;
		}

		@Override
		public boolean stop() {
			return true;
		}

		/** Always returns false. */
		@Override
		public boolean isEnabled() {
			return false;
		}

		@Override
		public void collectMetrics(Request<?> pRequest, Response<?> pResponse) {}
	};
}
