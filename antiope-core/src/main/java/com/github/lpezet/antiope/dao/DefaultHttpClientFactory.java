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
package com.github.lpezet.antiope.dao;

import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;

import com.github.lpezet.antiope.APIClientException;
import com.github.lpezet.antiope.be.APIConfiguration;

/**
 * @author luc
 */
public class DefaultHttpClientFactory implements IHttpClientFactory {

	private static final String SSL = "SSL";
	private static final String HTTPS = "https";
	private static final String HTTP = "http";

	@Override
	public HttpClient createHttpClient(APIConfiguration pConfiguration) {

		// Use a custom connection factory to customize the process of
		// initialization of outgoing HTTP connections. Beside standard connection
		// configuration parameters HTTP connection factory can define message
		// parser / writer routines to be employed by individual connections.
		HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> oConnFactory = new ManagedHttpClientConnectionFactory(new DefaultHttpRequestWriterFactory(), new DefaultHttpResponseParserFactory());

		SSLContext oSslContext = null;
		X509HostnameVerifier oHostnameVerifier = null;
		if (pConfiguration.isCheckSSLCertificates()) {
			oSslContext = SSLContexts.createSystemDefault();
			oHostnameVerifier = new BrowserCompatHostnameVerifier();
		} else {
			final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		        @Override
		        public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
		        }
		        @Override
		        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
		        }
		        @Override
		        public X509Certificate[] getAcceptedIssuers() {
		            return null;
		        }
		    } };
		    
		    // Install the all-trusting trust manager
			try {
			    final SSLContext sslContext = SSLContext.getInstance( SSL );
			    sslContext.init( null, trustAllCerts, new java.security.SecureRandom() ); 
			    // Create an ssl socket factory with our all-trusting manager
			    //final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			    oSslContext = sslContext;
			} catch (NoSuchAlgorithmException e) {
				throw new APIClientException(e);
			} catch (KeyManagementException e) {
				throw new APIClientException(e);
			}
		    oHostnameVerifier = new AllowAllHostnameVerifier();
		}
		
		// Create a registry of custom connection socket factories for supported
		// protocol schemes.
		Registry<ConnectionSocketFactory> oSocketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register(HTTP, PlainConnectionSocketFactory.INSTANCE)
				.register(HTTPS, new SSLConnectionSocketFactory(oSslContext, oHostnameVerifier))
				.build();

		// Use custom DNS resolver to override the system DNS resolution.
		DnsResolver oDnsResolver = new SystemDefaultDnsResolver(); /* {
			@Override
			public InetAddress[] resolve(final String host) throws UnknownHostException {
				if (host.equalsIgnoreCase("myhost")) {
					return new InetAddress[] { InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }) };
				} else {
					return super.resolve(host);
				}
			}
		};*/

		// Create a connection manager with custom configuration.
		PoolingHttpClientConnectionManager oConnManager = new PoolingHttpClientConnectionManager(oSocketFactoryRegistry, oConnFactory, oDnsResolver);

		// Create socket configuration
		SocketConfig oSocketConfig = SocketConfig.custom()
				.setTcpNoDelay(true)
				.setSoTimeout(pConfiguration.getSocketTimeout())
				.build();

		// Configure the connection manager to use socket configuration either
		// by default or for a specific host.
		oConnManager.setDefaultSocketConfig(oSocketConfig);
		// connManager.setSocketConfig(new HttpHost("somehost", 80), oSocketConfig);

		// Create message constraints
		MessageConstraints oMessageConstraints = MessageConstraints.custom()
				.setMaxHeaderCount(200)
				.setMaxLineLength(2000)
				.build();
		// Create connection configuration
		ConnectionConfig oConnectionConfig = ConnectionConfig.custom()
				.setMalformedInputAction(CodingErrorAction.IGNORE)
				.setUnmappableInputAction(CodingErrorAction.IGNORE)
				.setCharset(Consts.UTF_8)
				.setMessageConstraints(oMessageConstraints)
				.build();
		// Configure the connection manager to use connection configuration either
		// by default or for a specific host.
		oConnManager.setDefaultConnectionConfig(oConnectionConfig);
		// connManager.setConnectionConfig(new HttpHost("somehost", 80), ConnectionConfig.DEFAULT);

		// Configure total max or per route limits for persistent connections
		// that can be kept in the pool or leased by the connection manager.
		oConnManager.setMaxTotal(100);
		oConnManager.setDefaultMaxPerRoute(10);
		//oConnManager.setMaxPerRoute(new HttpRoute(new HttpHost("somehost", 80)), 20);

		// Use custom cookie store if necessary.
		CookieStore oCookieStore = new BasicCookieStore();
		// Use custom credentials provider if necessary.
		//
		// Create global request configuration
		RequestConfig oDefaultRequestConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.BEST_MATCH)
				//.setExpectContinueEnabled(true)			// WARNING: setting it to true slows things down by 4s!!!!
				.setStaleConnectionCheckEnabled(true)
				.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
				.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
				.setConnectTimeout(pConfiguration.getConnectionTimeout())
				.build();

		CredentialsProvider oCredentialsProvider = new BasicCredentialsProvider();
		HttpHost oProxy = null;

		if (pConfiguration.getProxyHost() != null && pConfiguration.getProxyPort() > 0) {
			String proxyHost = pConfiguration.getProxyHost();
			int proxyPort = pConfiguration.getProxyPort();
			String proxyUsername = pConfiguration.getProxyUsername();
			String proxyPassword = pConfiguration.getProxyPassword();
			String proxyDomain = pConfiguration.getProxyDomain();
			String proxyWorkstation = pConfiguration.getProxyWorkstation();

			oProxy = new HttpHost(proxyHost, proxyPort);

			if (proxyUsername != null && proxyPassword != null) {
				oCredentialsProvider.setCredentials(
						new AuthScope(proxyHost, proxyPort),
						new NTCredentials(proxyUsername, proxyPassword, proxyWorkstation, proxyDomain));
			}
		}

		// Create an HttpClient with the given custom dependencies and configuration.
		CloseableHttpClient oHttpClient = HttpClients.custom()
				.setConnectionManager(oConnManager)
				.setDefaultCookieStore(oCookieStore)
				.setDefaultCredentialsProvider(oCredentialsProvider)
				.setProxy(oProxy)
				.setDefaultRequestConfig(oDefaultRequestConfig)
				.build();
		
		return oHttpClient;
		/*
		RequestConfig oRequestConfig = RequestConfig.custom()
				.setConnectTimeout(pConfiguration.getConnectionTimeout())
				.setSocketTimeout(pConfiguration.getSocketTimeout())
				.setStaleConnectionCheckEnabled(true)
				.build();
		*/
	}
}
