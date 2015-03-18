/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * 
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.github.lpezet.antiope.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

/**
 * @author luc
 *
 */
public class StaxUnmarshallerContext {

	private XMLEvent mCurrentEvent;
    private final XMLEventReader mEventReader;

    public final Stack<String> mStack = new Stack<String>();
    private String mStackString = "";

    private Map<String, String> mMetadata = new HashMap<String, String>();
    private List<MetadataExpression> mMetadataExpressions = new ArrayList<MetadataExpression>();

    private Iterator<?> mAttributeIterator;
    private final Map<String, String> mHeaders;

    /**
     * Constructs a new unmarshaller context using the specified source of XML events.
     *
     * @param pEventReader
     *            The source of XML events for this unmarshalling context.
     */
    public StaxUnmarshallerContext(XMLEventReader pEventReader) {
        this(pEventReader, null);
    }

    /**
     * Constructs a new unmarshaller context using the specified source of XML
     * events, and a set of response headers.
     *
     * @param pEventReader
     *            The source of XML events for this unmarshalling context.
     * @param pHeaders
     *            The set of response headers associated with this unmarshaller
     *            context.
     */
    public StaxUnmarshallerContext(XMLEventReader pEventReader, Map<String, String> pHeaders) {
        this.mEventReader = pEventReader;
        this.mHeaders = pHeaders;
    }

    /**
     * Returns the value of the header with the specified name from the
     * response, or null if not present.
     *
     * @param pHeader
     *            The name of the header to lookup.
     *
     * @return The value of the header with the specified name from the
     *         response, or null if not present.
     */
    public String getHeader(String pHeader) {
        if (mHeaders == null) return null;

        return mHeaders.get(pHeader);
    }

    /**
     * Returns the text contents of the current element being parsed.
     *
     * @return The text contents of the current element being parsed.
     * @throws XMLStreamException XMLStreamException
     */
    public String readText() throws XMLStreamException {
        if (mCurrentEvent.isAttribute()) {
            Attribute attribute = (Attribute)mCurrentEvent;
            return attribute.getValue();
        }

        StringBuilder sb = new StringBuilder();
        while (true) {
            XMLEvent event = mEventReader.peek();
            if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
                mEventReader.nextEvent();
                sb.append(event.asCharacters().getData());
            } else if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {
                return sb.toString();
            } else {
                throw new RuntimeException("Encountered unexpected event: " + event.toString());
            }
        }
    }

    /**
     * Returns the element depth of the parser's current position in the XML
     * document being parsed.
     *
     * @return The element depth of the parser's current position in the XML
     *         document being parsed.
     */
    public int getCurrentDepth() {
        return mStack.size();
    }

    /**
     * Tests the specified expression against the current position in the XML
     * document being parsed.
     *
     * @param pExpression
     *            The psuedo-xpath expression to test.
     * @return True if the expression matches the current document position,
     *         otherwise false.
     */
    public boolean testExpression(String pExpression) {
        if (pExpression.equals(".")) return true;
        return mStackString.endsWith(pExpression);
    }

    /**
     * Tests the specified expression against the current position in the XML
     * document being parsed, and restricts the expression to matching at the
     * specified stack depth.
     *
     * @param pExpression
     *            The psuedo-xpath expression to test.
     * @param pStartingStackDepth
     *            The depth in the stack representing where the expression must
     *            start matching in order for this method to return true.
     *
     * @return True if the specified expression matches the current position in
     *         the XML document, starting from the specified depth.
     */
    public boolean testExpression(String pExpression, int pStartingStackDepth) {
        if (pExpression.equals(".")) return true;

        int index = -1;
        while ((index = pExpression.indexOf("/", index + 1)) > -1) {
            // Don't consider attributes a new depth level
            if (pExpression.charAt(index + 1) != '@') {
                pStartingStackDepth++;
            }
        }


        return (pStartingStackDepth == getCurrentDepth()
                && mStackString.endsWith("/" + pExpression));
    }

    /**
     * Returns true if this unmarshaller context is at the very beginning of a
     * source document (i.e. no data has been parsed from the document yet).
     *
     * @return true if this unmarshaller context is at the very beginning of a
     *         source document (i.e. no data has been parsed from the document
     *         yet).
     * @throws XMLStreamException XMLStreamException
     */
    public boolean isStartOfDocument() throws XMLStreamException {
        return mEventReader.peek().isStartDocument();
    }

    /**
     * Returns the next XML event for the document being parsed.
     *
     * @return The next XML event for the document being parsed.
     *
     * @throws XMLStreamException XMLStreamException
     */
    public XMLEvent nextEvent() throws XMLStreamException {
        if (mAttributeIterator != null && mAttributeIterator.hasNext()) {
            mCurrentEvent = (XMLEvent)mAttributeIterator.next();
        } else {
            mCurrentEvent = mEventReader.nextEvent();
        }

        if (mCurrentEvent.isStartElement()) {
            mAttributeIterator = mCurrentEvent.asStartElement().getAttributes();
        }

        updateContext(mCurrentEvent);

        if (mEventReader.hasNext()) {
            XMLEvent nextEvent = mEventReader.peek();
            if (nextEvent != null && nextEvent.isCharacters()) {
                for (MetadataExpression metadataExpression : mMetadataExpressions) {
                    if (testExpression(metadataExpression.expression, metadataExpression.targetDepth)) {
                        mMetadata.put(metadataExpression.key, nextEvent.asCharacters().getData());
                    }
                }
            }
        }

        return mCurrentEvent;
    }

    /**
     * Returns any metadata collected through metadata expressions while this
     * context was reading the XML events from the XML document.
     *
     * @return A map of any metadata collected through metadata expressions
     *         while this context was reading the XML document.
     */
    public Map<String, String> getMetadata() {
        return mMetadata;
    }

    /**
     * Registers an expression, which if matched, will cause the data for the
     * matching element to be stored in the metadata map under the specified
     * key.
     *
     * @param pExpression
     *            The expression an element must match in order for it's data to
     *            be pulled out and stored in the metadata map.
     * @param pTargetDepth
     *            The depth in the XML document where the expression match must
     *            start.
     * @param pStorageKey
     *            The key under which to store the matching element's data.
     */
    public void registerMetadataExpression(String pExpression, int pTargetDepth, String pStorageKey) {
        mMetadataExpressions.add(new MetadataExpression(pExpression, pTargetDepth, pStorageKey));
    }
    
    public XMLEventReader getEventReader() {
		return mEventReader;
	}


    /*
     * Private Interface
     */

    /**
     * Simple container for the details of a metadata expression this
     * unmarshaller context is looking for.
     */
    private static class MetadataExpression {
        public String expression;
        public int targetDepth;
        public String key;

        public MetadataExpression(String pExpression, int pTargetDepth, String pKey) {
            this.expression = pExpression;
            this.targetDepth = pTargetDepth;
            this.key = pKey;
        }
    }

    private void updateContext(XMLEvent pEvent) {
        if (pEvent == null) return;

        if (pEvent.isEndElement()) {
            mStack.pop();
            mStackString = "";
            for (String s : mStack) {
                mStackString += "/" + s;
            }
        } else if (pEvent.isStartElement()) {
            mStack.push(pEvent.asStartElement().getName().getLocalPart());
            mStackString += "/" + pEvent.asStartElement().getName().getLocalPart();
        } else if (pEvent.isAttribute()) {
            Attribute attribute = (Attribute)pEvent;
            mStackString = "";
            for (String s : mStack) {
                mStackString += "/" + s;
            }
            mStackString += "/@" + attribute.getName().getLocalPart();
        }
    }

}
