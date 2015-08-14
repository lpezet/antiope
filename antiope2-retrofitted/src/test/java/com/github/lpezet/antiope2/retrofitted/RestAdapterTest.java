// Copyright 2013 Square, Inc.
package com.github.lpezet.antiope2.retrofitted;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import rx.Observable;
import rx.functions.Action1;

import com.github.lpezet.antiope2.dao.http.HttpResponse;
import com.github.lpezet.antiope2.dao.http.IHttpNetworkIO;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;
import com.github.lpezet.antiope2.retrofitted.AntiopeError;
import com.github.lpezet.antiope2.retrofitted.Callback;
import com.github.lpezet.antiope2.retrofitted.RestAdapter;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Body;
import com.github.lpezet.antiope2.retrofitted.annotation.http.GET;
import com.github.lpezet.antiope2.retrofitted.annotation.http.POST;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Streaming;
import com.github.lpezet.antiope2.retrofitted.converter.Converter;
import com.github.lpezet.antiope2.retrofitted.converter.GsonConverter;
import com.google.gson.JsonParseException;

public class RestAdapterTest {

	private static class SimpleHttpResponse extends HttpResponse {

		public SimpleHttpResponse(IHttpRequest pRequest, int pStatusCode, String pContent) {
			super(pRequest);
			setStatusCode(pStatusCode);
			setContent(new ByteArrayInputStream(pContent.getBytes()));
		}

		public SimpleHttpResponse(IHttpRequest pRequest, int pStatusCode, String pStatusText, String pContent) {
			super(pRequest);
			setStatusCode(pStatusCode);
			setStatusText(pStatusText);
			if (pContent != null) {
				setContent(new ByteArrayInputStream(pContent.getBytes()));
				addHeader("Content-Length", "" + pContent.getBytes().length);
			}
		}

	}

	private interface Example {
		@com.github.lpezet.antiope2.retrofitted.annotation.http.Headers("Foo: Bar")
		@GET("/")
		String something();

		@com.github.lpezet.antiope2.retrofitted.annotation.http.Headers("Foo: Bar")
		@GET("/")
		void something(Callback<String> callback);

		@GET("/")
		IHttpResponse direct();

		@GET("/")
		void direct(Callback<IHttpResponse> callback);

		@GET("/")
		@Streaming
		IHttpResponse streaming();

		@POST("/")
		Observable<String> observable(@Body String body);
	}

	private interface InvalidExample extends Example {
	}

	// @Rule public final MockWebServerRule server = new MockWebServerRule();

	private Example										example;
	private Converter									converter;
	private IHttpNetworkIO<IHttpRequest, IHttpResponse>	client;

	@Before
	public void setUp() {
		// HttpClient oHttpClient = HttpClients.createDefault();
		// ApacheHttpClientNetworkIO client = new ApacheHttpClientNetworkIO(oHttpClient);
		client = mock(IHttpNetworkIO.class);

		converter = spy(new GsonConverter());

		example = new RestAdapter.Builder() //
				.client(client)
				/*
				.callbackExecutor(new Executor() {
					@Override
					public void execute(Runnable pCommand) {
						pCommand.run();
					}
				})
				*/
				.executorService(Executors.newCachedThreadPool())
				.endpoint("http://example.com")
				.converter(converter)
				.build()
				.create(Example.class);
	}

	@Test
	public void objectMethodsStillWork() {
		assertThat(example.hashCode()).isNotZero();
		assertThat(example.equals(this)).isFalse();
		assertThat(example.toString()).isNotEmpty();
	}

	@Test
	public void interfaceWithExtendIsNotSupported() {
		try {
			new RestAdapter.Builder().endpoint("http://foo/").build().create(InvalidExample.class);
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(e).hasMessage("Interface definitions must not extend other interfaces.");
		}
	}

	@Test
	public void http204SkipsConverter() throws Exception {
		onHttpRequestReturn(204, "Nothin", null);
		// server.enqueue(new MockResponse().setStatus("HTTP/1.1 204 Nothin"));
		assertThat(example.something()).isNull();
		verifyNoMoreInteractions(converter);
	}

	private void onHttpRequestReturn(final int pStatusCode, final String pStatusText, final String pContent) throws Exception {
		when(client.perform(Mockito.any(IHttpRequest.class))).then(new Answer<IHttpResponse>() {
			@Override
			public IHttpResponse answer(InvocationOnMock pInvocation) throws Throwable {
				IHttpRequest oRequest = (IHttpRequest) pInvocation.getArguments()[0];
				return new SimpleHttpResponse(oRequest, pStatusCode, pStatusText, pContent);
			}
		});
	}

	private void onHttpRequestReturn(String pContent) throws Exception {
		onHttpRequestReturn(200, "Ok", pContent);
	}

	@Test
	public void http204Response() throws Exception {
		onHttpRequestReturn(204, "Nothin", null);
		IHttpResponse response = example.direct();
		assertThat(response.getStatusCode()).isEqualTo(204);
	}

	@Test
	public void http204WithBodyThrows() throws Exception {
		onHttpRequestReturn(204, "Nothin", "Hey");
		try {
			example.something();
			fail();
		} catch (AntiopeError e) {
			assertThat(e).hasMessage("204 response must not include body.");
			Throwable cause = e.getCause();
			assertThat(cause).isInstanceOf(IllegalStateException.class);
			assertThat(cause).hasMessage("204 response must not include body.");
		}
	}

	@Test
	public void http205SkipsConverter() throws Exception {
		onHttpRequestReturn(204, "Nothin", null);
		assertThat(example.something()).isNull();
		verifyNoMoreInteractions(converter);
	}

	@Test
	public void http205Response() throws Exception {
		onHttpRequestReturn(205, "Nothin", null);
		IHttpResponse response = example.direct();
		assertThat(response.getStatusCode()).isEqualTo(205);
	}

	@Test
	public void http205WithBodyThrows() throws Exception {
		onHttpRequestReturn(205, "Nothin", "Hey");
		try {
			example.something();
			fail();
		} catch (AntiopeError e) {
			assertThat(e).hasMessage("205 response must not include body.");
			Throwable cause = e.getCause();
			assertThat(cause).isInstanceOf(IllegalStateException.class);
			assertThat(cause).hasMessage("205 response must not include body.");
		}
	}

	@Test
	public void successfulRequestResponseWhenMimeTypeMissing() throws Exception {
		onHttpRequestReturn("Hi");
		String string = example.something();
		assertThat(string).isEqualTo("Hi");
	}

	@Test
	public void malformedResponseThrowsConversionException() throws Exception {
		onHttpRequestReturn("{");
		try {
			example.something();
			fail();
		} catch (AntiopeError e) {
			assertThat(e.getKind()).isEqualTo(AntiopeError.Kind.UNEXPECTED);
			assertThat(e.getResponse().getStatusCode()).isEqualTo(200);
			assertThat(e.getCause()).isInstanceOf(JsonParseException.class);
			//TODO: WHY????
			//assertThat(e.getResponse().getContent()).isNull();
		}
	}

	@Test
	public void errorResponseThrowsHttpError() throws Exception {
		onHttpRequestReturn(500, "Broken", null);
		// server.enqueue(new MockResponse().setStatus("HTTP/1.1 500 Broken"));

		try {
			example.something();
			fail();
		} catch (AntiopeError e) {
			assertThat(e.getKind()).isEqualTo(AntiopeError.Kind.HTTP);
			assertThat(e.getResponse().getStatusCode()).isEqualTo(500);
			assertThat(e.getSuccessType()).isEqualTo(String.class);
		}
	}

	// TODO: How????
	@Ignore
	@Test
	public void clientExceptionThrowsNetworkError() throws Exception {
		// server.enqueue(new MockResponse().setBody("Hi").setSocketPolicy(DISCONNECT_AT_START));
		// onHttpRequestReturn(500, "Broken", null);
		try {
			example.something();
			fail();
		} catch (AntiopeError e) {
			assertThat(e.getKind()).isEqualTo(AntiopeError.Kind.NETWORK);
		}
	}

	private static void assertBody(InputStream body, String expected) {
		assertThat(body).isNotNull();
		try {
			String oBody = IOUtils.toString(body);
			assertThat(oBody).isEqualTo(expected);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void getResponseDirectly() throws Exception {
		// server.enqueue(new MockResponse().setBody("Hey"));
		onHttpRequestReturn("Hey");
		IHttpResponse response = example.direct();
		assertBody(response.getContent(), "Hey");
	}

	// TODO: How????
	@Ignore
	@Test
	public void streamingResponse() throws Exception {
		// server.enqueue(new MockResponse().setBody("Hey").setBodyDelay(500, MILLISECONDS));
		onHttpRequestReturn("Hey");

		IHttpResponse response = example.streaming();
		long startNs = System.nanoTime();
		// response.body().string();
		long tookNs = System.nanoTime() - startNs;
		assertThat(tookNs).isGreaterThanOrEqualTo(500);
	}

	@Test
	public void getResponseDirectlyAsync() throws Exception {
		// server.enqueue(new MockResponse().setBody("Hey"));
		onHttpRequestReturn("Hey");
		final AtomicReference<IHttpResponse> responseRef = new AtomicReference<IHttpResponse>();
		final CountDownLatch latch = new CountDownLatch(1);
		example.direct(new Callback<IHttpResponse>() {
			@Override
			public void success(IHttpResponse response, IHttpResponse response2) {
				responseRef.set(response);
				latch.countDown();
			}

			@Override
			public void failure(AntiopeError error) {
				throw new AssertionError();
			}
		});
		assertTrue(latch.await(1, TimeUnit.SECONDS));

		assertBody(responseRef.get().getContent(), "Hey");
	}

	@Test
	public void getAsync() throws Exception {
		// server.enqueue(new MockResponse().setBody("Hey"));
		onHttpRequestReturn("Hey");

		final AtomicReference<String> bodyRef = new AtomicReference<String>();
		final CountDownLatch latch = new CountDownLatch(1);
		example.something(new Callback<String>() {
			@Override
			public void success(String body, IHttpResponse response) {
				bodyRef.set(body);
				latch.countDown();
			}

			@Override
			public void failure(AntiopeError error) {
				throw new AssertionError();
			}
		});
		assertTrue(latch.await(1, TimeUnit.SECONDS));

		assertThat(bodyRef.get()).isEqualTo("Hey");
	}

	@Test
	public void errorAsync() throws Exception {
		// server.enqueue(new MockResponse().setStatus("HTTP/1.1 500 Broken!").setBody("Hey"));
		onHttpRequestReturn(500, "Broken!", "Hey");
		final AtomicReference<AntiopeError> errorRef = new AtomicReference<AntiopeError>();
		final CountDownLatch latch = new CountDownLatch(1);
		example.something(new Callback<String>() {
			@Override
			public void success(String s, IHttpResponse response) {
				throw new AssertionError();
			}

			@Override
			public void failure(AntiopeError error) {
				errorRef.set(error);
				latch.countDown();
			}
		});
		assertTrue(latch.await(1, TimeUnit.SECONDS));

		AntiopeError error = errorRef.get();
		assertThat(error.getResponse().getStatusCode()).isEqualTo(500);
		assertThat(error.getResponse().getStatusText()).isEqualTo("Broken!");
		assertThat(error.getSuccessType()).isEqualTo(String.class);
		assertThat(error.getBody()).isEqualTo("Hey");
	}

	@Test
	public void observableCallsOnNext() throws Exception {
		// server.enqueue(new MockResponse().setBody("hello"));
		onHttpRequestReturn("hello");

		final AtomicReference<String> bodyRef = new AtomicReference<String>();
		final CountDownLatch latch = new CountDownLatch(1);
		example.observable("Howdy").subscribe(new Action1<String>() {
			@Override
			public void call(String body) {
				bodyRef.set(body);
				latch.countDown();
			}
		});
		assertTrue(latch.await(1, TimeUnit.SECONDS));

		assertThat(bodyRef.get()).isEqualTo("hello");
	}

	@Test
	public void observableCallsOnError() throws Exception {
		// server.enqueue(new MockResponse().setResponseCode(500));
		onHttpRequestReturn(500, null, null);

		final AtomicReference<Throwable> errorRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);
		example.observable("Howdy").subscribe(new Action1<String>() {
			@Override
			public void call(String s) {
				throw new AssertionError();
			}
		}, new Action1<Throwable>() {
			@Override
			public void call(Throwable throwable) {
				errorRef.set(throwable);
				latch.countDown();
			}
		});
		assertTrue(latch.await(1, TimeUnit.SECONDS));

		AntiopeError error = (AntiopeError) errorRef.get();
		assertThat(error.getResponse().getStatusCode()).isEqualTo(500);
		assertThat(error.getSuccessType()).isEqualTo(String.class);
	}

}
