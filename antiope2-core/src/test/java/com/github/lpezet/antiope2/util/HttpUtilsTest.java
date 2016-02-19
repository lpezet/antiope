/**
 * 
 */
package com.github.lpezet.antiope2.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Luc Pezet
 *
 */
public class HttpUtilsTest {
	
	@Test
	public void getPort() {
		assertEquals(0, HttpUtils.getPort( "http://test.com/") );
		assertEquals(80, HttpUtils.getPort("http://test.com:80/abcd") );
		assertEquals(8443, HttpUtils.getPort( "https://test.com:8443/abcd") );
		assertEquals(443, HttpUtils.getPort( "https://test.com:443") );
	}
	
	@Test
	public void getHostAndPort() {
		assertEquals("test.com", HttpUtils.getHostAndPort( "http://test.com/") );
		assertEquals("test.com:80", HttpUtils.getHostAndPort("http://test.com:80/abcd") );
		assertEquals("test.com:8443", HttpUtils.getHostAndPort( "https://test.com:8443/abcd") );
		assertEquals("test.com:443", HttpUtils.getHostAndPort( "https://test.com:443") );
	}

	@Test
	public void getHost() {
		assertEquals("test.com", HttpUtils.getHost( "http://test.com/") );
		assertEquals("test.com", HttpUtils.getHost( "http://test.com") );
		assertEquals("test.com", HttpUtils.getHost( "https://test.com/abcd") );
		assertEquals("test.com", HttpUtils.getHost( "http://test.com:80/abcd") );
		assertEquals("test.com", HttpUtils.getHost( "https://test.com:8443/abcd") );
		assertEquals("test.com", HttpUtils.getHost( "https://test.com:443") );
	}
}
