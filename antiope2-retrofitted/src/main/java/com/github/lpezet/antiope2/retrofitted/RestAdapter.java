/**
 * 
 */
package com.github.lpezet.antiope2.retrofitted;

import static com.github.lpezet.antiope2.retrofitted.Utils.checkNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.lpezet.antiope2.dao.http.IHttpNetworkIO;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;
import com.github.lpezet.antiope2.dao.http.Signer;
import com.github.lpezet.antiope2.dao.http.StubSigner;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;
import com.github.lpezet.antiope2.metrics.StubMetricsCollector;
import com.github.lpezet.antiope2.retrofitted.converter.Converter;

/**
 * @author Luc Pezet
 */
public class RestAdapter {

	private final Map<Class<?>, Map<Method, MethodInfo>>		serviceMethodInfoCache	= new LinkedHashMap<Class<?>, Map<Method, MethodInfo>>();

	private final String										mEndpoint;
	private final IHttpNetworkIO<IHttpRequest, IHttpResponse>	mNetworkIO;
	// private Executor mCallbackExecutor;
	private final ExecutorService								mExecutorService;
	private final Converter										mConverter;
	private final ErrorHandler									mErrorHandler;
	private IMetricsCollector									mMetricsCollector;
	private final Signer										mSigner;
	private final RequestInterceptor							mRequestInterceptor;

	private RestAdapter(String endpoint, IHttpNetworkIO pNetworkIO, ExecutorService executorService, Converter converter, ErrorHandler errorHandler, Signer pSigner, RequestInterceptor pRequestInterceptor) {
		mEndpoint = endpoint;
		mNetworkIO = pNetworkIO;
		// mCallbackExecutor = callbackExecutor;
		mConverter = converter;
		mErrorHandler = errorHandler;
		mExecutorService = executorService;
		mSigner = pSigner;
		mRequestInterceptor = pRequestInterceptor;
	}

	/** Create an implementation of the API defined by the specified {@code service} interface. */
	@SuppressWarnings("unchecked")
	public <T> T create(Class<T> service) {
		Utils.validateServiceClass(service);
		return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
				new RestHandler(mEndpoint, mNetworkIO, mMetricsCollector, mExecutorService, mConverter, getMethodInfoCache(service), mErrorHandler, mSigner, mRequestInterceptor));
	}

	Map<Method, MethodInfo> getMethodInfoCache(Class<?> service) {
		synchronized (serviceMethodInfoCache) {
			Map<Method, MethodInfo> methodInfoCache = serviceMethodInfoCache.get(service);
			if (methodInfoCache == null) {
				methodInfoCache = new LinkedHashMap<Method, MethodInfo>();
				serviceMethodInfoCache.put(service, methodInfoCache);
			}
			return methodInfoCache;
		}
	}

	static MethodInfo getMethodInfo(Map<Method, MethodInfo> cache, Method method) {
		synchronized (cache) {
			MethodInfo methodInfo = cache.get(method);
			if (methodInfo == null) {
				methodInfo = new MethodInfo(method);
				cache.put(method, methodInfo);
			}
			return methodInfo;
		}
	}

	/**
	 * Build a new {@link RestAdapter}.
	 * <p>
	 * Calling {@link #endpoint} is required before calling {@link #build()}. All other methods are
	 * optional.
	 */
	public static class Builder {
		private String				endpoint;
		private IHttpNetworkIO		client;
		// private Executor callbackExecutor;
		private ExecutorService		executorService;
		private Converter			converter;
		private ErrorHandler		errorHandler;
		private IMetricsCollector	metricsCollector;
		private Signer				signer;
		private RequestInterceptor 	requestInterceptor;

		/** API endpoint URL. */
		public Builder endpoint(String url) {
			this.endpoint = checkNotNull(url, "endpoint == null");
			return this;
		}

		/** The HTTP client used for requests. */
		public Builder client(IHttpNetworkIO client) {
			this.client = checkNotNull(client, "client == null");
			return this;
		}

		public Builder metricsCollector(IMetricsCollector pMetricsCollector) {
			metricsCollector = pMetricsCollector;
			return this;
		}
		
		public Builder requestInterceptor(RequestInterceptor pInterceptor) {
			requestInterceptor = pInterceptor;
			return this;
		}

		/**
		 * Executor on which any {@link Callback} methods will be invoked. If this argument is
		 * {@code null} then callback methods will be run on the same thread as the HTTP client.
		 */
		/*
		 * public Builder callbackExecutor(Executor callbackExecutor) {
		 * if (callbackExecutor == null) {
		 * callbackExecutor = new Utils.SynchronousExecutor();
		 * }
		 * this.callbackExecutor = callbackExecutor;
		 * return this;
		 * }
		 */
		public Builder executorService(ExecutorService executorService) {
			this.executorService = executorService;
			return this;
		}

		/** The converter used for serialization and deserialization of objects. */
		public Builder converter(Converter converter) {
			this.converter = checkNotNull(converter, "converter == null");
			return this;
		}

		/**
		 * The error handler allows you to customize the type of exception thrown for errors on
		 * synchronous requests.
		 */
		public Builder errorHandler(ErrorHandler errorHandler) {
			this.errorHandler = checkNotNull(errorHandler, "errorHandler == null");
			return this;
		}

		/** Create the {@link RestAdapter} instances. */
		public RestAdapter build() {
			checkNotNull(endpoint, "Endpoint required.");
			ensureSaneDefaults();
			return new RestAdapter(endpoint, client, executorService, converter, errorHandler, signer, requestInterceptor);
		}

		private void ensureSaneDefaults() {
			if (converter == null) {
				converter = Platform.get().defaultConverter();
			}
			if (client == null) {
				client = Platform.get().defaultClient();
			}
			if (signer == null) {
				signer = new StubSigner();
			}
			if (requestInterceptor == null) {
				requestInterceptor = RequestInterceptor.NOP_INTERCEPTOR;
			}
			// if (callbackExecutor == null) {
			// callbackExecutor = Platform.get().defaultCallbackExecutor();
			// }
			if (metricsCollector == null) {
				metricsCollector = new StubMetricsCollector();
			}
			if (executorService == null) {
				// TODO: Replace by Platgorm.get().defaultExecutorService();
				executorService = Executors.newCachedThreadPool();
			}
			if (errorHandler == null) {
				errorHandler = ErrorHandler.DEFAULT;
			}
		}
	}
}
