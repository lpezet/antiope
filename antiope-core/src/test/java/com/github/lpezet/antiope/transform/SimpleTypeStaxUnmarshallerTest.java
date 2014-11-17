/**
 * 
 */
package com.github.lpezet.antiope.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import com.github.lpezet.antiope.util.DateUtils;

/**
 * @author Luc Pezet
 *
 */
public class SimpleTypeStaxUnmarshallerTest {
	
	private XMLInputFactory mXMLInputFactory = XMLInputFactory.newInstance();
	
	protected StaxUnmarshallerContext createContext(String pXML) throws Exception {
		StringReader oSReader = new StringReader(pXML);
		XMLEventReader oReader = mXMLInputFactory.createXMLEventReader(oSReader);
		return new StaxUnmarshallerContext(oReader);
	}

	@Test
	public void string() throws Exception {
		StaxUnmarshallerContext oCtxt = createContext("<a>Hello World</a>");
		String oActual = null;
		while(true) {
			XMLEvent oEvent = oCtxt.nextEvent();
			if (oEvent.isStartElement()) {
				oActual = SimpleTypeStaxUnmarshallers.StringStaxUnmarshaller.getInstance().unmarshall(oCtxt);
				break;
			}
		}
		assertEquals("Hello World", oActual);
	}
	
	@Test
	public void integer() throws Exception {
		StaxUnmarshallerContext oCtxt = createContext("<a>123</a>");
		Integer oActual = null;
		while(true) {
			XMLEvent oEvent = oCtxt.nextEvent();
			if (oEvent.isStartElement()) {
				oActual = SimpleTypeStaxUnmarshallers.IntegerStaxUnmarshaller.getInstance().unmarshall(oCtxt);
				break;
			}
		}
		assertNotNull(oActual);
		assertEquals(123, oActual.intValue());
	}
	
	@Test
	public void bigDecimal() throws Exception {
		StaxUnmarshallerContext oCtxt = createContext("<a>123.123</a>");
		BigDecimal oActual = null;
		while(true) {
			XMLEvent oEvent = oCtxt.nextEvent();
			if (oEvent.isStartElement()) {
				oActual = SimpleTypeStaxUnmarshallers.BigDecimalStaxUnmarshaller.getInstance().unmarshall(oCtxt);
				break;
			}
		}
		assertNotNull(oActual);
		assertEquals(0, new BigDecimal("123.123").compareTo(oActual));
	}
	
	@Test
	public void bigInteger() throws Exception {
		StaxUnmarshallerContext oCtxt = createContext("<a>123</a>");
		BigInteger oActual = null;
		while(true) {
			XMLEvent oEvent = oCtxt.nextEvent();
			if (oEvent.isStartElement()) {
				oActual = SimpleTypeStaxUnmarshallers.BigIntegerStaxUnmarshaller.getInstance().unmarshall(oCtxt);
				break;
			}
		}
		assertNotNull(oActual);
		assertEquals(0, new BigInteger("123").compareTo(oActual));
	}
	
	@Test
	public void bool() throws Exception {
		StaxUnmarshallerContext oCtxt = createContext("<a>true</a>");
		Boolean oActual = null;
		while(true) {
			XMLEvent oEvent = oCtxt.nextEvent();
			if (oEvent.isStartElement()) {
				oActual = SimpleTypeStaxUnmarshallers.BooleanStaxUnmarshaller.getInstance().unmarshall(oCtxt);
				break;
			}
		}
		assertNotNull(oActual);
		assertTrue(oActual.booleanValue());
	}
	
	@Test
	public void byteBuffer() throws Exception {
		String oText = "Hello";
		byte[] oEncoded = Base64.encodeBase64(oText.getBytes());
		StaxUnmarshallerContext oCtxt = createContext("<a>" + new String(oEncoded) + "</a>");
		ByteBuffer oActual = null;
		while(true) {
			XMLEvent oEvent = oCtxt.nextEvent();
			if (oEvent.isStartElement()) {
				oActual = SimpleTypeStaxUnmarshallers.ByteBufferStaxUnmarshaller.getInstance().unmarshall(oCtxt);
				break;
			}
		}
		assertNotNull(oActual);
		assertEquals(oText, new String( oActual.array() ) );
	}
	
	@Test
	public void byt() throws Exception {
		StaxUnmarshallerContext oCtxt = createContext("<a>1</a>");
		Byte oActual = null;
		while(true) {
			XMLEvent oEvent = oCtxt.nextEvent();
			if (oEvent.isStartElement()) {
				oActual = SimpleTypeStaxUnmarshallers.ByteStaxUnmarshaller.getInstance().unmarshall(oCtxt);
				break;
			}
		}
		assertNotNull(oActual);
		assertEquals(1, oActual.byteValue());
	}
	
	@Test
	public void dat() throws Exception {
		String oSDate = "2013-11-29T23:37:45.123Z";
		StaxUnmarshallerContext oCtxt = createContext("<a>" + oSDate + "</a>");
		Date oActual = null;
		while(true) {
			XMLEvent oEvent = oCtxt.nextEvent();
			if (oEvent.isStartElement()) {
				oActual = SimpleTypeStaxUnmarshallers.DateStaxUnmarshaller.getInstance().unmarshall(oCtxt);
				break;
			}
		}
		assertNotNull(oActual);
		DateUtils oDU = new DateUtils();
		Date oExpected = oDU.parseIso8601Date(oSDate);
		assertEquals(oExpected.getTime(), oActual.getTime());
	}
	
	@Test
	public void doubl() throws Exception {
		StaxUnmarshallerContext oCtxt = createContext("<a>123.123</a>");
		Double oActual = null;
		while(true) {
			XMLEvent oEvent = oCtxt.nextEvent();
			if (oEvent.isStartElement()) {
				oActual = SimpleTypeStaxUnmarshallers.DoubleStaxUnmarshaller.getInstance().unmarshall(oCtxt);
				break;
			}
		}
		assertNotNull(oActual);
		assertEquals(123.123d, oActual.doubleValue(), 0.0);
	}
	
	@Test
	public void floa() throws Exception {
		StaxUnmarshallerContext oCtxt = createContext("<a>123.1</a>");
		Float oActual = null;
		while(true) {
			XMLEvent oEvent = oCtxt.nextEvent();
			if (oEvent.isStartElement()) {
				oActual = SimpleTypeStaxUnmarshallers.FloatStaxUnmarshaller.getInstance().unmarshall(oCtxt);
				break;
			}
		}
		assertNotNull(oActual);
		assertEquals(123.1f, oActual.floatValue(), 0.0);
	}
	
	@Test
	public void lon() throws Exception {
		StaxUnmarshallerContext oCtxt = createContext("<a>123</a>");
		Long oActual = null;
		while(true) {
			XMLEvent oEvent = oCtxt.nextEvent();
			if (oEvent.isStartElement()) {
				oActual = SimpleTypeStaxUnmarshallers.LongStaxUnmarshaller.getInstance().unmarshall(oCtxt);
				break;
			}
		}
		assertNotNull(oActual);
		assertEquals(123l, oActual.longValue());
	}
	
	
	
}
