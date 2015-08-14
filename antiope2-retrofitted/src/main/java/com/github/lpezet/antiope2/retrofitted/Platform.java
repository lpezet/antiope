/*
 * Copyright (C) 2013 Square, Inc.
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

import java.util.concurrent.Executor;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.github.lpezet.antiope2.dao.http.IHttpNetworkIO;
import com.github.lpezet.antiope2.dao.http.apache.ApacheHttpClientNetworkIO;
import com.github.lpezet.antiope2.retrofitted.converter.Converter;
import com.github.lpezet.antiope2.retrofitted.converter.GsonConverter;

class Platform {
  private static final Platform PLATFORM = findPlatform();

  static final boolean HAS_RX_JAVA = hasRxJavaOnClasspath();

  static Platform get() {
    return PLATFORM;
  }

  private static Platform findPlatform() {
    try {
      Class.forName("android.os.Build");
      if (Build.VERSION.SDK_INT != 0) {
        return new Android();
      }
    } catch (ClassNotFoundException ignored) {
    }

    return new Platform();
  }

  Converter defaultConverter() {
    return new GsonConverter();
  }

  Executor defaultCallbackExecutor() {
    return new Utils.SynchronousExecutor();
  }

  IHttpNetworkIO defaultClient() {
	  RequestConfig defaultRequestConfig = RequestConfig.custom()
			    .setSocketTimeout(5000)
			    .setConnectTimeout(5000)
			    .setConnectionRequestTimeout(5000)
			    .setStaleConnectionCheckEnabled(true)
			    .build();
	  HttpClient oHttpClient = HttpClients.custom()
			  .setDefaultRequestConfig(defaultRequestConfig)
			  .build();
    return new ApacheHttpClientNetworkIO( oHttpClient );
  }

  /** Provides sane defaults for operation on Android. */
  private static class Android extends Platform {
    @Override Executor defaultCallbackExecutor() {
      return new Executor() {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override public void execute(Runnable r) {
          handler.post(r);
        }
      };
    }
  }

  private static boolean hasRxJavaOnClasspath() {
    try {
      Class.forName("rx.Observable");
      return true;
    } catch (ClassNotFoundException ignored) {
    }
    return false;
  }
}
