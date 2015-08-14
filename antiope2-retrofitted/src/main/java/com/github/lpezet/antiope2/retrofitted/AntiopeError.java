/*
 * Copyright (C) 2012 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lpezet.antiope2.retrofitted;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import android.provider.ContactsContract.Presence;

import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;
import com.github.lpezet.antiope2.retrofitted.converter.Converter;

public class AntiopeError extends RuntimeException {
	public static AntiopeError networkFailure(String url, IOException exception) {
		return new AntiopeError(exception.getMessage(), url, null, null, null, Kind.NETWORK,
				exception);
	}

	public static AntiopeError networkFailure(IHttpRequest pRequest, IOException exception) {
		return new AntiopeError(exception.getMessage(), getUrl(pRequest), null, null, null, Kind.NETWORK,
				exception);
	}

	private static String getUrl(IHttpRequest pRequest) {
		String url = pRequest.getEndpoint().toString() + pRequest.getResourcePath();
		return url;
	}

	public static AntiopeError httpError(IHttpResponse response, Converter converter, Type successType) {
		String message = response.getStatusCode() + " " + response.getStatusText();
		return new AntiopeError(message, getUrl(response.getHttpRequest()), response, converter,
				successType, Kind.HTTP, null);
	}

	public static AntiopeError networkError(IHttpResponse response, IOException exception) {
		// response = response.newBuilder().body(null).build(); // Remove any body.
		System.out.println("Request = " + response.getHttpRequest());
		return new AntiopeError(exception.getMessage(), getUrl(response.getHttpRequest()), null, null, null, Kind.NETWORK, exception);
	}

	public static AntiopeError unexpectedError(IHttpResponse response, Throwable exception) {
		// response = response.newBuilder().body(null).build(); // Remove any body.
		return new AntiopeError(exception.getMessage(), getUrl(response.getHttpRequest()), response, null, null, Kind.UNEXPECTED, exception);
	}
	
	public static AntiopeError unexpectedError(IHttpRequest request, Throwable exception) {
		// response = response.newBuilder().body(null).build(); // Remove any body.
		return new AntiopeError(exception.getMessage(), getUrl(request), null, null, null, Kind.UNEXPECTED, exception);
	}

	public static AntiopeError unexpectedError(String url, Throwable exception) {
		return new AntiopeError(exception.getMessage(), url, null, null, null, Kind.UNEXPECTED,
				exception);
	}

	/** Identifies the event kind which triggered a {@link AntiopeError}. */
	public enum Kind {
		/** An {@link IOException} occurred while communicating to the server. */
		NETWORK,
		/** A non-200 HTTP status code was received from the server. */
		HTTP,
		/**
		 * An internal error occurred while attempting to execute a request. It is best practice to
		 * re-throw this exception so your application crashes.
		 */
		UNEXPECTED
	}

	private final String		url;
	private final IHttpResponse	response;
	private final Converter		converter;
	private final Type			successType;
	private final Kind			kind;

	AntiopeError(String message, String url, IHttpResponse response, Converter converter,
			Type successType, Kind kind, Throwable exception) {
		super(message, exception);
		this.url = url;
		this.response = response;
		this.converter = converter;
		this.successType = successType;
		this.kind = kind;
	}

	/** The request URL which produced the error. */
	public String getUrl() {
		return url;
	}

	/** Response object containing status code, headers, body, etc. */
	public IHttpResponse getResponse() {
		return response;
	}

	/** The event kind which triggered this error. */
	public Kind getKind() {
		return kind;
	}

	/**
	 * HTTP response body converted to the type declared by either the interface method return type
	 * or the generic type of the supplied {@link Callback} parameter. {@code null} if there is no
	 * response.
	 *
	 * @throws RuntimeException
	 *             if unable to convert the body to the {@link #getSuccessType() success
	 *             type}.
	 */
	public Object getBody() {
		return getBodyAs(successType);
	}

	/**
	 * The type declared by either the interface method return type or the generic type of the
	 * supplied {@link Callback} parameter.
	 */
	public Type getSuccessType() {
		return successType;
	}

	/**
	 * HTTP response body converted to specified {@code type}. {@code null} if there is no response.
	 *
	 * @throws RuntimeException
	 *             if unable to convert the body to the specified {@code type}.
	 */
	public Object getBodyAs(Type type) {
		if (response == null) {
			return null;
		}

		InputStream body = response.getContent();
		if (body == null) {
			return null;
		}
		try {
			return converter.deserialize(response.getContent(), type);
		} catch (IOException e) {
			throw new RuntimeException(e); // Body is a Buffer, can't be a real IO exception.
		}
	}
}
