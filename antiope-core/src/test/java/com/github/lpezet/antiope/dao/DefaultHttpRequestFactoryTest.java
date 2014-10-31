/**
 * 
 */
package com.github.lpezet.antiope.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URI;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.be.APIWebServiceRequest;

/**
 * @author lucpezet
 *
 */
public class DefaultHttpRequestFactoryTest {
	
	private static class DummyRequest extends APIWebServiceRequest {
		
	}

	@Test
	public void postMethodWithPayload() throws Exception {
		DefaultHttpRequestFactory oFactory = new DefaultHttpRequestFactory();
		
		Request<DummyRequest> oRequest = new DefaultRequest<DummyRequest>(new DummyRequest(), "Dummy");
		oRequest.setEndpoint(URI.create("http://dummy.com"));
		byte[] oContent = "{toto:{}}".getBytes();
		oRequest.setContent(new ByteArrayInputStream(oContent));
		oRequest.addHeader("Content-Length", Integer.toString(oContent.length));
		
		APIConfiguration oConfiguration = new APIConfiguration();
		
		HttpContext oHttpContext = new BasicHttpContext();
		
		ExecutionContext oExecutionContext = new ExecutionContext();
		
		HttpRequestBase oActual = oFactory.createHttpRequest(oRequest, oConfiguration, oHttpContext, oExecutionContext);
		assertNotNull(oActual);
		assertTrue(oActual instanceof HttpPost);
		HttpPost oPost = (HttpPost) oActual;
		assertNotNull(oPost.getEntity());
		RepeatableInputStreamRequestEntity oEntity = (RepeatableInputStreamRequestEntity) oPost.getEntity();
		assertEquals(oContent.length, oEntity.getContentLength());
	}
}
