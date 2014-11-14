/**
 * 
 */
package com.github.lpezet.antiope.util;

import static org.junit.Assert.assertNotNull;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Luc Pezet
 *
 */
public class DateUtilsTest {
	
	private DateUtils mDateUtils;
	
	@Before
	public void setup() {
		mDateUtils = new DateUtils();
	}

	@Test
	public void parseIso8601Date() throws ParseException {
		assertNotNull( mDateUtils.parseIso8601Date("2013-11-29T20:37:45.123Z") );
	}
	
	@Test
	public void parseRfc822Date() throws ParseException {
		assertNotNull( mDateUtils.parseRfc822Date("Thu, 13 Nov 2014 20:41:45 EST") );
	}
	
	@Test
	public void parseCompressedIso8601Date() throws ParseException {
		assertNotNull( mDateUtils.parseCompressedIso8601Date("20141113T204636Z") );
	}
}
