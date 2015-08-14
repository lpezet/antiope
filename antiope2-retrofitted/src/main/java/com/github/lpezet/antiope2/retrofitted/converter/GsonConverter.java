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
package com.github.lpezet.antiope2.retrofitted.converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import com.google.gson.Gson;

/**
 * A {@link Converter} which uses GSON for serialization and deserialization of entities.
 */
public class GsonConverter implements Converter {
	private final Gson		gson;
	private final Charset	charset;

	// private final MediaType mediaType;

	/**
	 * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
	 * decoding from JSON (when no charset is specified by a header) will use UTF-8.
	 */
	public GsonConverter() {
		this(new Gson());
	}

	/**
	 * Create an instance using the supplied {@link Gson} object for conversion. Encoding to JSON
	 * and
	 * decoding from JSON (when no charset is specified by a header) will use UTF-8.
	 */
	public GsonConverter(Gson gson) {
		this(gson, Charset.forName("UTF-8"));
	}

	/**
	 * Create an instance using the supplied {@link Gson} object for conversion. Encoding to JSON
	 * and
	 * decoding from JSON (when no charset is specified by a header) will use the specified charset.
	 */
	public GsonConverter(Gson gson, Charset charset) {
		if (gson == null) throw new NullPointerException("gson == null");
		if (charset == null) throw new NullPointerException("charset == null");
		this.gson = gson;
		this.charset = charset;
		// this.mediaType = MediaType.parse("application/json; charset=" + charset.name());
	}

	@Override
	public Object deserialize(InputStream pBody, Type pType) throws IOException {
		Charset charset = this.charset;
		return gson.fromJson(new InputStreamReader(pBody, charset), pType);

	}

	@Override
	public InputStream serialize(Object pBody, Type pType) {
		String oJson = gson.toJson(pBody, pType);
		return new ByteArrayInputStream(oJson.getBytes());
	}
}
