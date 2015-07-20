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
package com.github.lpezet.antiope2.dao.http.apache;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HttpContext;

import com.github.lpezet.antiope2.APIClientException;
import com.github.lpezet.antiope2.be.IAPIConfiguration;
import com.github.lpezet.antiope2.dao.ExecutionContext;
import com.github.lpezet.antiope2.dao.http.Header;
import com.github.lpezet.antiope2.dao.http.HttpExecutionContext;
import com.github.lpezet.antiope2.dao.http.HttpMethodName;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.RepeatableInputStreamRequestEntity;
import com.github.lpezet.antiope2.metrics.APIRequestMetrics;
import com.github.lpezet.antiope2.util.HttpUtils;


/**
 * @author Luc Pezet
 *
 */
public class ApacheHttpClientMarshaller implements IApacheHttpClientMarshaller {
	
	private static final String	QMARK				= "?";
	private static final String	USER_AGENT			= "User-Agent";
	private static final String	COLON				= ":";
	private static final String	CONTENT_LENGTH		= "Content-Length";
	private static final String	HOST				= "Host";
	private static final String	CONTENT_TYPE		= "Content-Type";
	private static final String	SPACE				= " ";
	private static final String	DEFAULT_ENCODING	= "UTF-8";

	@Override
	public ApacheHttpRequest perform(IHttpRequest pSource) throws Exception {
		ApacheHttpRequest oResult = new ApacheHttpRequest();
		HttpContext oHttpContext = HttpClientContext.create();
		oResult.setHttpContext(oHttpContext);
		
		ExecutionContext oExecutionContext = pSource.getExecutionContext();
		oResult.setExecutionContext(oExecutionContext);
		
		// Signing request if any signer.
		if (canSign(oExecutionContext)) doSign(oExecutionContext, pSource);

		URI oEndpoint = pSource.getEndpoint();
		/*
		 * HttpClient cannot handle url in pattern of "http://host//path", so we
		 * have to escape the double-slash between endpoint and resource-path
		 * into "/%2F"
		 */
		String oUri = HttpUtils.appendUri(oEndpoint.toString(), pSource.getResourcePath(), true);
		String oEncodedParams = HttpUtils.encodeParameters(pSource.getParameters());

		/*
		 * For all non-POST requests, and any POST requests that already have a
		 * payload, we put the encoded params directly in the URI, otherwise,
		 * we'll put them in the POST request's payload.
		 */
		boolean oRequestHasNoPayload = pSource.getContent() == null;
		boolean oRequestIsPost = HttpMethodName.POST.name().equalsIgnoreCase( pSource.getHttpMethod() );
		boolean oPutParamsInUri = !oRequestIsPost || oRequestHasNoPayload;
		if (oEncodedParams != null && oPutParamsInUri) {
			oUri += QMARK + oEncodedParams;
		}

		HttpRequestBase oHttpRequest;
		if (HttpMethodName.POST.name().equalsIgnoreCase( pSource.getHttpMethod() )) {
			HttpPost oPostMethod = new HttpPost(oUri);

			/*
			 * If there isn't any payload content to include in this request,
			 * then try to include the POST parameters in the query body,
			 * otherwise, just use the query string. For all API Query services,
			 * the best behavior is putting the params in the request body for
			 * POST requests, but we can't do that for S3.
			 */
			if (pSource.getContent() == null && oEncodedParams != null) {
				oPostMethod.setEntity(newStringEntity(oEncodedParams));
			} else if (pSource.getContent() != null) {
				oPostMethod.setEntity(new RepeatableInputStreamRequestEntity( pSource ));
			}
			oHttpRequest = oPostMethod;
		} else if (HttpMethodName.PUT.name().equalsIgnoreCase( pSource.getHttpMethod() )) {
			HttpPut putMethod = new HttpPut(oUri);
			
			/*
			 * Enable 100-continue support for PUT operations, since this is
			 * where we're potentially uploading large amounts of data and want
			 * to find out as early as possible if an operation will fail. We
			 * don't want to do this for all operations since it will cause
			 * extra latency in the network interaction.
			 */
			putMethod.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);

			//if (pPreviousEntity != null) {
			//	putMethod.setEntity(pPreviousEntity);
			//} else 
			if (pSource.getContent() != null) {
				HttpEntity entity = new RepeatableInputStreamRequestEntity( pSource );
				if (pSource.getFirstHeaderValue(CONTENT_LENGTH) == null) {
					entity = newBufferedHttpEntity(entity);
				}
				putMethod.setEntity( entity );
			}
			
			oHttpRequest = putMethod;
		} else if (HttpMethodName.GET.name().equalsIgnoreCase( pSource.getHttpMethod() )) {
			oHttpRequest = new HttpGet(oUri);
		} else if (HttpMethodName.DELETE.name().equalsIgnoreCase( pSource.getHttpMethod() )) {
			oHttpRequest = new HttpDelete(oUri);
		} else if (HttpMethodName.HEAD.name().equalsIgnoreCase( pSource.getHttpMethod() )) {
			oHttpRequest = new HttpHead(oUri);
		} else {
			throw new APIClientException("Unknown HTTP method name: " + pSource.getHttpMethod());
		}

		configureHeaders(oHttpRequest, pSource, oExecutionContext);

		oResult.setHttpRequestBase(oHttpRequest);
		
		return oResult;
	}
	
	private void doSign(ExecutionContext pExecutionContext, IHttpRequest pSource) {
		pExecutionContext.getMetrics().startEvent(APIRequestMetrics.RequestSigningTime);
		try {
			((HttpExecutionContext) pExecutionContext).getSigner().sign(pSource, pExecutionContext.getCredentials());
		} finally {
			pExecutionContext.getMetrics().endEvent(APIRequestMetrics.RequestSigningTime);
		}
	}

	private boolean canSign(ExecutionContext pExecutionContext) {
		if (!(pExecutionContext instanceof HttpExecutionContext)) return false;
		HttpExecutionContext oHEC = (HttpExecutionContext) pExecutionContext;
		return oHEC.getSigner() != null && oHEC.getCredentials() != null;
	}

	/** Configures the headers in the specified Apache HTTP request. */
	private void configureHeaders(HttpRequestBase pHttpRequest, IHttpRequest pRequest, ExecutionContext pContext) {
		/*
		 * Apache HttpClient omits the port number in the Host header (even if
		 * we explicitly specify it) if it's the default port for the protocol
		 * in use. To ensure that we use the same Host header in the request and
		 * in the calculated string to sign (even if Apache HttpClient changed
		 * and started honoring our explicit host with endpoint), we follow this
		 * same behavior here and in the QueryString signer.
		 */
		URI endpoint = pRequest.getEndpoint();
		String hostHeader = endpoint.getHost();
		if (HttpUtils.isUsingNonDefaultPort(endpoint)) {
			hostHeader += COLON + endpoint.getPort();
		}
		pHttpRequest.addHeader(HOST, hostHeader);

		// Copy over any other headers already in our request
		for (Header h : pRequest.getHeaders()) {
			/*
			 * HttpClient4 fills in the Content-Length header and complains if
			 * it's already present, so we skip it here. We also skip the Host
			 * header to avoid sending it twice, which will interfere with some
			 * signing schemes.
			 */
			if (h.getName().equalsIgnoreCase(CONTENT_LENGTH) || h.getName().equalsIgnoreCase(HOST)) continue;

			pHttpRequest.addHeader(h.getName(), h.getValue());
		}

		/* Set content type and encoding */
		if (pHttpRequest.getHeaders(CONTENT_TYPE) == null || pHttpRequest.getHeaders(CONTENT_TYPE).length == 0) {
			pHttpRequest.addHeader(CONTENT_TYPE, "application/x-www-form-urlencoded; " + "charset=" + DEFAULT_ENCODING.toLowerCase());
		}

		// Override the user agent string specified in the client params if the
		// context requires it
		if (pContext != null && pContext.getContextUserAgent() != null) {
			pHttpRequest.addHeader( USER_AGENT, createUserAgentString(pRequest.getExecutionContext().getAPIConfiguration(), pContext.getContextUserAgent()));
		}
	}

	/**
	 * Appends the given user-agent string to the client's existing one and
	 * returns it.
	 */
	private String createUserAgentString(IAPIConfiguration pConfiguration, String pContextUserAgent) {
		if (pConfiguration.getUserAgent().contains(pContextUserAgent)) {
			return pConfiguration.getUserAgent();
		} else {
			return pConfiguration.getUserAgent() + SPACE + pContextUserAgent;
		}
	}

	/**
	 * Utility function for creating a new StringEntity and wrapping any errors
	 * as an TSGClientException.
	 * 
	 * @param s
	 *            The string contents of the returned HTTP entity.
	 * @return A new StringEntity with the specified contents.
	 */
	private HttpEntity newStringEntity(String s) {
		try {
			return new StringEntity(s);
		} catch (UnsupportedEncodingException e) {
			throw new APIClientException("Unable to create HTTP entity: " + e.getMessage(), e);
		}
	}

	/**
	 * Utility function for creating a new BufferedEntity and wrapping any
	 * errors as an TSGClientException.
	 * 
	 * @param entity
	 *            The HTTP entity to wrap with a buffered HTTP entity.
	 * @return A new BufferedHttpEntity wrapping the specified entity.
	 */
	private HttpEntity newBufferedHttpEntity(HttpEntity entity) {
		try {
			return new BufferedHttpEntity(entity);
		} catch (IOException e) {
			throw new APIClientException("Unable to create HTTP entity: " + e.getMessage(), e);
		}
	}

}
