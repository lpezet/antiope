/*
 * Copyright (C) 2012 Square, Inc.
 * Copyright (C) 2007 The Guava Authors
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
import java.util.concurrent.Executor;

import com.github.lpezet.antiope2.dao.http.IHttpResponse;

final class Utils {
  static <T> T checkNotNull(T object, String message) {
    if (object == null) {
      throw new NullPointerException(message);
    }
    return object;
  }

  /**
   * Replace a link Response with an identical copy whose body is backed by a
   * link Buffer rather than a link Source.
   */
  static IHttpResponse readBodyToBytesIfNecessary(IHttpResponse response) throws IOException {
   /*
	  final ResponseBody body = response.body();
    if (body == null) {
      return response;
    }

    BufferedSource source = body.source();
    final Buffer buffer = new Buffer();
    buffer.writeAll(source);
    source.close();

    return response.newBuilder()
        .body(new ResponseBody() {
          @Override public MediaType contentType() {
            return body.contentType();
          }

          @Override public long contentLength() {
            return buffer.size();
          }

          @Override public BufferedSource source() {
            return buffer.clone();
          }
        })
        .build();
        */
	  return response;
  }

  static <T> void validateServiceClass(Class<T> service) {
    if (!service.isInterface()) {
      throw new IllegalArgumentException("Only interface endpoint definitions are supported.");
    }
    // Prevent API interfaces from extending other interfaces. This not only avoids a bug in
    // Android (http://b.android.com/58753) but it forces composition of API declarations which is
    // the recommended pattern.
    if (service.getInterfaces().length > 0) {
      throw new IllegalArgumentException("Interface definitions must not extend other interfaces.");
    }
  }

  static class SynchronousExecutor implements Executor {
    @Override public void execute(Runnable runnable) {
      runnable.run();
    }
  }

  private Utils() {
    // No instances.
  }
}
