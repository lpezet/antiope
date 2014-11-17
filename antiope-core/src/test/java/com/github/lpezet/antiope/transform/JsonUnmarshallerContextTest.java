/**
 * 
 */
package com.github.lpezet.antiope.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * @author Luc Pezet
 *
 */
public class JsonUnmarshallerContextTest {

	@Test
	public void walkthrough() throws Exception {
		String oJson = "{ \"a\" : { \"b\" : { \"c\" : \"Hello World\" } } }";
		JsonParser oParser = new JsonFactory().createJsonParser(oJson);
		JsonUnmarshallerContext oCtxt = new JsonUnmarshallerContext( oParser );
		
		assertTrue(oCtxt.isStartOfDocument());
		assertEquals(0, oCtxt.getCurrentDepth());
		assertEquals(JsonToken.START_OBJECT, oCtxt.nextToken());
		assertEquals(JsonToken.FIELD_NAME, oCtxt.nextToken());
		assertEquals(1, oCtxt.getCurrentDepth());
		assertEquals("a", oCtxt.readText());
		assertEquals(JsonToken.START_OBJECT, oCtxt.nextToken());
		assertEquals(JsonToken.FIELD_NAME, oCtxt.nextToken());
		assertEquals(2, oCtxt.getCurrentDepth());
		assertEquals("b", oCtxt.readText());
		assertEquals(JsonToken.START_OBJECT, oCtxt.nextToken());
		assertEquals(JsonToken.FIELD_NAME, oCtxt.nextToken());
		assertEquals(3, oCtxt.getCurrentDepth());
		assertEquals("c", oCtxt.readText());
		assertEquals(JsonToken.VALUE_STRING, oCtxt.nextToken());
		assertEquals(JsonToken.END_OBJECT, oCtxt.nextToken()); // c
		assertEquals(JsonToken.END_OBJECT, oCtxt.nextToken()); // b
		assertEquals(JsonToken.END_OBJECT, oCtxt.nextToken()); // a
	}
	
	@Test
	public void testExpression() throws Exception {
		String oJson = "{ \"a\": { \"b\": { \"c\": \"Hello World\" } } }";
		JsonParser oParser = new JsonFactory().createJsonParser(oJson);
		JsonUnmarshallerContext oCtxt = new JsonUnmarshallerContext( oParser );
		
		assertTrue( oCtxt.testExpression(".") );
		oCtxt.nextToken();
		oCtxt.nextToken();
		assertTrue( oCtxt.testExpression("a") );
		oCtxt.nextToken();
		oCtxt.nextToken();
		assertTrue( oCtxt.testExpression("b") );
		oCtxt.nextToken();
		oCtxt.nextToken();
		assertTrue( oCtxt.testExpression("c") );
	}
}
