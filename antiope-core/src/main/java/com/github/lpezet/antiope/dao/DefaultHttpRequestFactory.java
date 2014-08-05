/**
 * 
 */
package com.github.lpezet.antiope.dao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HttpContext;

import com.github.lpezet.antiope.APIClientException;
import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.metrics.APIRequestMetrics;
import com.github.lpezet.antiope.util.HttpUtils;

/**
 * @author luc
 */
public class DefaultHttpRequestFactory implements IHttpRequestFactory {

	private static final String	QMARK				= "?";
	private static final String	USER_AGENT			= "User-Agent";
	private static final String	COLON				= ":";
	private static final String	CONTENT_LENGTH		= "Content-Length";
	private static final String	HOST				= "Host";
	private static final String	CONTENT_TYPE		= "Content-Type";
	private static final String	SPACE				= " ";
	private static final String	DEFAULT_ENCODING	= "UTF-8";

	@Override
	public HttpRequestBase createHttpRequest(Request<?> pRequest, APIConfiguration pConfiguration, HttpContext pHttpContext, ExecutionContext pExecutionContext) {
		// Signing request is any signer.
		if (pExecutionContext.getSigner() != null && pExecutionContext.getCredentials() != null) {
			pExecutionContext.getMetrics().startEvent(APIRequestMetrics.RequestSigningTime);
			try {
				pExecutionContext.getSigner().sign(pRequest, pExecutionContext.getCredentials());
			} finally {
				pExecutionContext.getMetrics().endEvent(APIRequestMetrics.RequestSigningTime);
			}
		}

		URI oEndpoint = pRequest.getEndpoint();
		/*
		 * HttpClient cannot handle url in pattern of "http://host//path", so we
		 * have to escape the double-slash between endpoint and resource-path
		 * into "/%2F"
		 */
		String oUri = HttpUtils.appendUri(oEndpoint.toString(), pRequest.getResourcePath(), true);
		String oEncodedParams = HttpUtils.encodeParameters(pRequest);

		/*
		 * For all non-POST requests, and any POST requests that already have a
		 * payload, we put the encoded params directly in the URI, otherwise,
		 * we'll put them in the POST request's payload.
		 */
		boolean oRequestHasNoPayload = pRequest.getContent() != null;
		boolean oRequestIsPost = pRequest.getHttpMethod() == HttpMethodName.POST;
		boolean oPutParamsInUri = !oRequestIsPost || oRequestHasNoPayload;
		if (oEncodedParams != null && oPutParamsInUri) {
			oUri += QMARK + oEncodedParams;
		}

		HttpRequestBase oHttpRequest;
		if (pRequest.getHttpMethod() == HttpMethodName.POST) {
			HttpPost oPostMethod = new HttpPost(oUri);

			/*
			 * If there isn't any payload content to include in this request,
			 * then try to include the POST parameters in the query body,
			 * otherwise, just use the query string. For all API Query services,
			 * the best behavior is putting the params in the request body for
			 * POST requests, but we can't do that for S3.
			 */
			if (pRequest.getContent() == null && oEncodedParams != null) {
				oPostMethod.setEntity(newStringEntity(oEncodedParams));
			} else if (pRequest.getContent() != null) {
				oPostMethod.setEntity(new RepeatableInputStreamRequestEntity( pRequest ));
			}
			oHttpRequest = oPostMethod;
		} else if (pRequest.getHttpMethod() == HttpMethodName.PUT) {
			HttpPut putMethod = new HttpPut(oUri);
			oHttpRequest = putMethod;

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
			if (pRequest.getContent() != null) {
				HttpEntity entity = new RepeatableInputStreamRequestEntity(pRequest);
				if (pRequest.getHeaders().get(CONTENT_LENGTH) == null) {
					entity = newBufferedHttpEntity(entity);
				}
				putMethod.setEntity( entity );
			}
		} else if (pRequest.getHttpMethod() == HttpMethodName.GET) {
			oHttpRequest = new HttpGet(oUri);
		} else if (pRequest.getHttpMethod() == HttpMethodName.DELETE) {
			oHttpRequest = new HttpDelete(oUri);
		} else if (pRequest.getHttpMethod() == HttpMethodName.HEAD) {
			oHttpRequest = new HttpHead(oUri);
		} else {
			throw new APIClientException("Unknown HTTP method name: " + pRequest.getHttpMethod());
		}

		configureHeaders(oHttpRequest, pRequest, pExecutionContext, pConfiguration);

		return oHttpRequest;
	}

	/** Configures the headers in the specified Apache HTTP request. */
	private void configureHeaders(HttpRequestBase pHttpRequest, Request<?> pRequest, ExecutionContext pContext, APIConfiguration pConfiguration) {
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
		for (Entry<String, String> entry : pRequest.getHeaders().entrySet()) {
			/*
			 * HttpClient4 fills in the Content-Length header and complains if
			 * it's already present, so we skip it here. We also skip the Host
			 * header to avoid sending it twice, which will interfere with some
			 * signing schemes.
			 */
			if (entry.getKey().equalsIgnoreCase(CONTENT_LENGTH) || entry.getKey().equalsIgnoreCase(HOST)) continue;

			pHttpRequest.addHeader(entry.getKey(), entry.getValue());
		}

		/* Set content type and encoding */
		if (pHttpRequest.getHeaders(CONTENT_TYPE) == null || pHttpRequest.getHeaders(CONTENT_TYPE).length == 0) {
			pHttpRequest.addHeader(CONTENT_TYPE, "application/x-www-form-urlencoded; " + "charset=" + DEFAULT_ENCODING.toLowerCase());
		}

		// Override the user agent string specified in the client params if the
		// context requires it
		if (pContext != null && pContext.getContextUserAgent() != null) {
			pHttpRequest.addHeader( USER_AGENT, createUserAgentString(pConfiguration, pContext.getContextUserAgent()));
		}
	}

	/**
	 * Appends the given user-agent string to the client's existing one and
	 * returns it.
	 */
	private String createUserAgentString(APIConfiguration pConfiguration, String pContextUserAgent) {
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
