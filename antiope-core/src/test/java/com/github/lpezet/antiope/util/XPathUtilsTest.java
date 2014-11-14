/**
 * 
 */
package com.github.lpezet.antiope.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Luc Pezet
 *
 */
public class XPathUtilsTest {
	
	private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

	protected Node createDocument(String pXML) throws Exception {
		Document oDoc = FACTORY.newDocumentBuilder().parse(new ByteArrayInputStream(pXML.getBytes()));
		return oDoc.getDocumentElement();
	}

	@Test
	public void documentFromInputStream() throws Exception {
		
	}
	
	@Test
	public void documentFromString() throws Exception {
		
	}
	
	@Test
	public void documentFromURL() throws Exception {
		
	}
	
	@Test
	public void asBoolean() throws Exception {
		Node oRoot = createDocument("<a><b><c>true</c></b></a>");
		Boolean oActual = XPathUtils.asBoolean("/a/b/c", oRoot);
		assertNotNull(oActual);
		assertTrue(oActual.booleanValue());
	}
	
	@Test
	public void asByte() throws Exception {
		
	}
	
	@Test
	public void asByteBuffer() throws Exception {
		
	}
	
	@Test
	public void asDate() throws Exception {
		
	}
	
	@Test
	public void asDouble() throws Exception {
		Node oRoot = createDocument("<a><b><c>123.4</c></b></a>");
		Double oActual = XPathUtils.asDouble("/a/b/c", oRoot);
		assertNotNull(oActual);
		assertEquals(123.4d, oActual.doubleValue(), 0.0);
	}
	
	@Test
	public void asFloat() throws Exception {
		Node oRoot = createDocument("<a><b><c>123.4</c></b></a>");
		Float oActual = XPathUtils.asFloat("/a/b/c", oRoot);
		assertNotNull(oActual);
		assertEquals(123.4f, oActual.floatValue(), 0.0);
	}
	
	@Test
	public void asInteger() throws Exception {
		Node oRoot = createDocument("<a><b><c>123</c></b></a>");
		Integer oActual = XPathUtils.asInteger("/a/b/c", oRoot);
		assertNotNull(oActual);
		assertEquals(123, oActual.intValue());
	}
	
	@Test
	public void asLong() throws Exception {
		Node oRoot = createDocument("<a><b><c>1234567890123</c></b></a>");
		Long oActual = XPathUtils.asLong("/a/b/c", oRoot);
		assertNotNull(oActual);
		assertEquals(1234567890123l, oActual.longValue());
	}
	
	@Test
	public void asNode() throws Exception {
		Node oRoot = createDocument("<a><b><c>1234567890123</c></b></a>");
		Node n = XPathUtils.asNode("/a/b/c", oRoot);
		assertNotNull( n );
		assertEquals("c", n.getNodeName());
	}
	@Test
	public void asString() throws Exception {
		Node oRoot = createDocument("<a><b><c>Hello world!</c></b></a>");
		String oActual = XPathUtils.asString("/a/b/c", oRoot);
		assertEquals("Hello world!", oActual);
	}
	
	@Test
	public void isEmpty() throws Exception {
		assertTrue( XPathUtils.isEmpty( null ) );
	}
	
	@Test
	public void nodeLength() throws Exception {
		Node oRoot = createDocument("<all><a/><b/><c/></all>");
		assertEquals(3, XPathUtils.nodeLength(oRoot.getChildNodes()));
	}
	
}
