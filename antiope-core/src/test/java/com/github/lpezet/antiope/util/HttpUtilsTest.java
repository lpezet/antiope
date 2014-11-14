/**
 * 
 */
package com.github.lpezet.antiope.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.github.lpezet.antiope.dao.DefaultRequest;
import com.github.lpezet.antiope.dao.HttpMethodName;
import com.github.lpezet.antiope.dao.Request;

/**
 * @author Luc Pezet
 *
 */
public class HttpUtilsTest {
	
	@Test
	public void space() {
		assertEquals("hello%20world", HttpUtils.urlEncode("hello world", false));
	}
	
	@Test
	public void wildcard() {
		assertEquals("hello%2Aworld", HttpUtils.urlEncode("hello*world", false));
	}
	
	@Test
	public void tilde() {
		assertEquals("hello~world", HttpUtils.urlEncode("hello~world", false));
	}
	
	@Test
	public void path() {
		assertEquals("hello/world", HttpUtils.urlEncode("hello/world", true));
		assertEquals("hello%2Fworld", HttpUtils.urlEncode("hello/world", false));
	}
	
	@Test
	public void isUsingNonDefaultPort() throws Exception {
		assertTrue( HttpUtils.isUsingNonDefaultPort( new URI("https://toto.com:8443") ) );
		assertFalse( HttpUtils.isUsingNonDefaultPort( new URI("https://toto.com") ) );
		
		assertTrue( HttpUtils.isUsingNonDefaultPort( new URI("http://toto.com:8080") ) );
		assertFalse( HttpUtils.isUsingNonDefaultPort( new URI("http://toto.com") ) );
		
	}
	
	@Test
	public void usePayloadForQueryParameters() {
		Request<Object> oRequest = new DefaultRequest<Object>(null);
		oRequest.setHttpMethod(HttpMethodName.POST);
		oRequest.setContent(null);
		assertTrue( HttpUtils.usePayloadForQueryParameters(oRequest) );
		
		// not a POST method
		oRequest = new DefaultRequest<Object>(null);
		oRequest.setHttpMethod(HttpMethodName.GET);
		assertFalse( HttpUtils.usePayloadForQueryParameters(oRequest) );
		
		// POST with content already
		oRequest = new DefaultRequest<Object>(null);
		oRequest.setHttpMethod(HttpMethodName.POST);
		oRequest.setContent(new ByteArrayInputStream("hello world".getBytes()));
		assertFalse( HttpUtils.usePayloadForQueryParameters(oRequest) );
		
	}
	
	@Test
	public void encodeParameters() {
		Request<Object> oRequest = new DefaultRequest<Object>(null);
		oRequest.addParameter("hello", "world");
		oRequest.addParameter("bye", "bye world");
		oRequest.addParameter("abc", "~!@#$%^&*()_+[]\\{}|;':\",./<>?");
		
		assertEquals("hello=world&abc=%7E%21%40%23%24%25%5E%26*%28%29_%2B%5B%5D%5C%7B%7D%7C%3B%27%3A%22%2C.%2F%3C%3E%3F&bye=bye+world", HttpUtils.encodeParameters(oRequest));
	}
	
	@Test
	public void appendUri() {
		assertEquals("http://toto.com/test", HttpUtils.appendUri("http://toto.com/", "/test"));
		assertEquals("http://toto.com/test", HttpUtils.appendUri("http://toto.com", "/test"));
		
		assertEquals("http://toto.com/url/%2Ftest", HttpUtils.appendUri("http://toto.com/url", "//test", true));
		
	}
}
