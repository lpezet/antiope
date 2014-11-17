/**
 * 
 */
package com.github.lpezet.antiope.transform;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

import org.junit.Test;

/**
 * @author Luc Pezet
 *
 */
public class StaxUnmarshallerContextTest {

	private XMLInputFactory mXMLInputFactory = XMLInputFactory.newInstance();

	@Test
	public void walkthrough() throws Exception {
		String oXML = "<a><b><c>Hello World</c></b></a>";
		StringReader oSReader = new StringReader(oXML);
		XMLEventReader oReader = mXMLInputFactory.createXMLEventReader(oSReader);
        StaxUnmarshallerContext oCtxt = new StaxUnmarshallerContext(oReader);
        
        assertEquals(0, oCtxt.getCurrentDepth());
        XMLEvent oEvent = oCtxt.nextEvent();
        assertEquals(XMLStreamConstants.START_DOCUMENT, oEvent.getEventType());
        oEvent = oCtxt.nextEvent();
        assertEquals(XMLStreamConstants.START_ELEMENT, oEvent.getEventType()); // a
        oEvent = oCtxt.nextEvent();
        assertEquals(XMLStreamConstants.START_ELEMENT, oEvent.getEventType()); // b
        oEvent = oCtxt.nextEvent();
        assertEquals(XMLStreamConstants.START_ELEMENT, oEvent.getEventType()); // c
        String oText = oCtxt.readText();
        assertEquals("Hello World", oText);
        /*
        oEvent = oCtxt.nextEvent();
        assertEquals(XMLStreamConstants.CHARACTERS, oEvent.getEventType()); // c
        */
        oEvent = oCtxt.nextEvent();
        assertEquals(XMLStreamConstants.END_ELEMENT, oEvent.getEventType()); // c
        oEvent = oCtxt.nextEvent();
        assertEquals(XMLStreamConstants.END_ELEMENT, oEvent.getEventType()); // b
        oEvent = oCtxt.nextEvent();
        assertEquals(XMLStreamConstants.END_ELEMENT, oEvent.getEventType()); // a
        oEvent = oCtxt.nextEvent();
        assertEquals(XMLStreamConstants.END_DOCUMENT, oEvent.getEventType());
	}
}
