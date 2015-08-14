/**
 * 
 */
package com.github.lpezet.antiope2.retrofitted;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.github.lpezet.antiope2.dao.http.HttpExecutionContext;
import com.github.lpezet.antiope2.dao.http.IHttpNetworkIO;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;
import com.github.lpezet.antiope2.metrics.BaseMetrics;
import com.github.lpezet.antiope2.metrics.IMetrics;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;
import com.github.lpezet.antiope2.retrofitted.converter.Converter;
import com.github.lpezet.java.patterns.worker.AsyncWorker;
import com.github.lpezet.java.patterns.worker.Callback;
import com.github.lpezet.java.patterns.worker.IAsyncResult;

/**
 * @author Luc Pezet
 */
public class RestHandler implements InvocationHandler {

	private final String										mEndpoint;
	private final Converter										mDefaultConverter;
	private final Map<Method, MethodInfo>						mMethodDetailsCache;
	private final ErrorHandler									mErrorHandler;
	private final IHttpNetworkIO<IHttpRequest, IHttpResponse>	mIO;
	private final AsyncWorker<IHttpRequest, IHttpResponse>		mAsyncIO;
	private final ExecutorService								mExecutorService;
	private final IMetricsCollector								mMetricsCollector;
	private RxSupport											mRxSupport;

	RestHandler(
			String pEndpoint,
			IHttpNetworkIO<IHttpRequest, IHttpResponse> pIO,
			IMetricsCollector pMetricsCollector,
			ExecutorService pExecutorService,
			Converter pDefaultConverter,
			Map<Method, MethodInfo> pMethodDetailsCache,
			ErrorHandler pErrorHandler) {
		mEndpoint = pEndpoint;
		mMetricsCollector = pMetricsCollector;
		mDefaultConverter = pDefaultConverter;
		mMethodDetailsCache = pMethodDetailsCache;
		mErrorHandler = pErrorHandler;
		mIO = pIO;
		mAsyncIO = new AsyncWorker<IHttpRequest, IHttpResponse>(pExecutorService, pIO);
		mExecutorService = pExecutorService;
	}

	@SuppressWarnings("unchecked")
	//
	@Override
	public Object invoke(Object pProxy, Method pMethod, final Object[] pArgs)
			throws Throwable {
		// If the method is a method from Object then defer to normal invocation.
		if (pMethod.getDeclaringClass() == Object.class) {
			return pMethod.invoke(this, pArgs);
		}

		MethodInfo methodInfo = getMethodInfo(mMethodDetailsCache, pMethod);
		IHttpRequest request = createRequest(methodInfo, pArgs);
		switch (methodInfo.getExecutionType()) {
			case SYNC:
				return invokeSync(methodInfo, request);
			case ASYNC:
				invokeAsync(methodInfo, request, (com.github.lpezet.antiope2.retrofitted.Callback) pArgs[pArgs.length - 1]);
				return null; // Async has void return type.
			case RX:
				return invokeRx(methodInfo, request);
			default:
				throw new IllegalStateException("Unknown response type: " + methodInfo.getExecutionType());
		}
	}

	private Object invokeRx(final MethodInfo pMethodInfo, final IHttpRequest pRequest) {
		if (mRxSupport == null) {
			if (Platform.HAS_RX_JAVA) {
				mRxSupport = new RxSupport();
			} else {
				throw new IllegalStateException("Found Observable return type but RxJava not present.");
			}
		}
		return mRxSupport.createRequestObservable(new RxSupport.Invoker() {
			@Override
			public void invoke(final Callback callback) {
				try {
					IAsyncResult<IHttpResponse> oAsyncResult = mAsyncIO.perform(pRequest);
					oAsyncResult.setCallback(new com.github.lpezet.java.patterns.worker.Callback<IHttpResponse>() {

						@Override
						public void onResult(IHttpResponse pResult) {
							try {
								Object result = createResult(pMethodInfo, pResult);
								callback.next(result);
							} catch (AntiopeError error) {
								callback.error(handleError(error));
							}
						}

						@Override
						public void onException(Exception e) {
							if (IOException.class.isInstance(e)) callback.next(AntiopeError.networkFailure(pRequest, (IOException) e));
							else callback.next(AntiopeError.unexpectedError(pRequest, e));
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
				/*
				 * Call call = client.newCall(request);
				 * call.enqueue(new com.squareup.okhttp.Callback() {
				 * @Override public void onFailure(Request request, IOException e) {
				 * callback.next(RetrofitError.networkFailure(request.urlString(), e));
				 * }
				 * @Override public void onResponse(Response response) {
				 * try {
				 * Object result = createResult(methodInfo, response);
				 * callback.next(result);
				 * } catch (RetrofitError error) {
				 * callback.error(handleError(error));
				 * }
				 * }
				 * });
				 */

			}
		});
	}

	private void invokeAsync(final MethodInfo pMethodInfo, final IHttpRequest pRequest, final com.github.lpezet.antiope2.retrofitted.Callback pCallback) throws Exception {
		IAsyncResult<IHttpResponse> oAsyncResult = mAsyncIO.perform(pRequest);
		oAsyncResult.setCallback(new Callback<IHttpResponse>() {

			@Override
			public void onResult(IHttpResponse pResult) {
				try {
					Object result = createResult(pMethodInfo, pResult);
					callResponse(pCallback, result, pResult);
				} catch (AntiopeError error) {
					callFailure(pCallback, error);
				}
			}

			@Override
			public void onException(Exception e) {
				if (IOException.class.isInstance(e)) callFailure(pCallback, AntiopeError.networkFailure(pRequest, (IOException) e));
				else callFailure(pCallback, AntiopeError.unexpectedError(pRequest, e));
			}
		});
	}

	private void callResponse(final com.github.lpezet.antiope2.retrofitted.Callback callback, final Object result, final IHttpResponse response) {
		callback.success(result, response);
		// TODO: Need it????
		/*
		 * callbackExecutor.execute(new Runnable() {
		 * @Override public void run() {
		 * callback.success(result, response);
		 * }
		 * });
		 */
	}

	private void callFailure(final com.github.lpezet.antiope2.retrofitted.Callback callback, AntiopeError error) {
		Throwable throwable = handleError(error);
		if (throwable != error) {
			IHttpResponse response = error.getResponse();
			if (response != null) {
				error = AntiopeError.unexpectedError(response, throwable);
			} else {
				error = AntiopeError.unexpectedError(error.getUrl(), throwable);
			}
		}
		final AntiopeError finalError = error;
		callback.failure(finalError);
		// TODO: Need it?????
		/*
		 * callbackExecutor.execute(new Runnable() {
		 * @Override public void run() {
		 * callback.failure(finalError);
		 * }
		 * });
		 */
	}

	private Object invokeSync(MethodInfo pMethodInfo, IHttpRequest pRequest) throws Throwable {
		try {
			IHttpResponse response = mIO.perform(pRequest);
			return createResult(pMethodInfo, response);
		} catch (IOException e) {
			throw handleError(AntiopeError.networkFailure(pRequest, e));
		} catch (AntiopeError error) {
			throw handleError(error);
		}
	}

	/**
	 * Create the object to return to the caller for a response.
	 *
	 * @throws AntiopeError
	 *             if any HTTP, network, or unexpected errors occurred.
	 */
	private Object createResult(MethodInfo methodInfo, IHttpResponse response) {
		try {
			return parseResult(methodInfo, response);
		} catch (AntiopeError error) {
			throw error; // Let our own errors pass through.
		} catch (IOException e) {
			e.printStackTrace();
			throw AntiopeError.networkError(response, e);
		} catch (Throwable t) {
			throw AntiopeError.unexpectedError(response, t);
		}
	}

	/**
	 * Parse the object to return to the caller from a response.
	 *
	 * @throws AntiopeError
	 *             on non-2xx response codes (kind = HTTP).
	 * @throws IOException
	 *             on network problems reading the response data.
	 * @throws RuntimeException
	 *             on malformed response data.
	 */
	private Object parseResult(MethodInfo methodInfo, IHttpResponse response) throws IOException {
		Type type = methodInfo.getResponseObjectType();
		Converter oConverter = getConverter(methodInfo);
		int statusCode = response.getStatusCode();
		if (statusCode < 200 || statusCode >= 300) {
			// response = Utils.readBodyToBytesIfNecessary(response);
			throw AntiopeError.httpError(response, oConverter, type);
		}

		if (type.equals(IHttpResponse.class)) {
			// TODO: Add support for Streaming
			/*
			 * if (!methodInfo.isStreaming()) {
			 * // Read the entire stream and replace with one backed by a byte[].
			 * response = Utils.readBodyToBytesIfNecessary(response);
			 * }
			 */
			return response;
		}
		InputStream body = response.getContent();
		if (statusCode == 204 || statusCode == 205) {
			// HTTP 204 No Content "...response MUST NOT include a message-body"
			// HTTP 205 Reset Content "...response MUST NOT include an entity"
			// String response.getHeaders().get("Content-Length");
			if (response.getContentLength() > 0) {
				throw new IllegalStateException(statusCode + " response must not include body.");
			}
			return null;
		}

		// ExceptionCatchingRequestBody wrapped = new ExceptionCatchingRequestBody(body);
		try {
			return oConverter.deserialize(body, type);
		} catch (RuntimeException e) {
			// If the underlying input stream threw an exception, propagate that rather than
			// indicating that it was a conversion exception.
			// if (wrapped.threwException()) {
			// throw wrapped.getThrownException();
			// }
			throw e;
		}
	}

	private Throwable handleError(AntiopeError error) {
		Throwable throwable = mErrorHandler.handleError(error);
		if (throwable == null) {
			return new IllegalStateException("Error handler returned null for wrapped exception.",
					error);
		}
		return throwable;
	}

	static MethodInfo getMethodInfo(Map<Method, MethodInfo> pCache, Method pMethod) {
		synchronized (pCache) {
			MethodInfo methodInfo = pCache.get(pMethod);
			if (methodInfo == null) {
				methodInfo = new MethodInfo(pMethod);
				pCache.put(pMethod, methodInfo);
			}
			return methodInfo;
		}
	}

	private IHttpRequest createRequest(MethodInfo pMethodInfo, Object[] pArgs) throws Exception {
		// Very Antiope2 specific
		IMetrics oMetrics = new BaseMetrics();
		HttpExecutionContext oContext = new HttpExecutionContext(oMetrics, mMetricsCollector);

		RequestBuilder requestBuilder = new RequestBuilder(mEndpoint, pMethodInfo, getConverter(pMethodInfo), oContext);
		requestBuilder.setArguments(pArgs);
		return requestBuilder.build();
	}

	private Converter getConverter(MethodInfo pMethodInfo) {
		return pMethodInfo.getConverter() == null ? mDefaultConverter : pMethodInfo.getConverter();
	}

}
