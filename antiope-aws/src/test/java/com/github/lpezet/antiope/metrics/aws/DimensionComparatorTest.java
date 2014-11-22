/**
 * 
 */
package com.github.lpezet.antiope.metrics.aws;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.amazonaws.services.cloudwatch.model.Dimension;

/**
 * @author Luc Pezet
 *
 */
public class DimensionComparatorTest {

	@Test
	public void equal() {
		Dimension d1 = new Dimension().withName("abc");
		Dimension d2 = new Dimension().withName("abc");
		assertEquals(0, DimensionComparator.INSTANCE.compare(d1, d2));
		
		assertEquals(0, DimensionComparator.INSTANCE.compare(null, null));
	}
	
	@Test
	public void less() {
		Dimension d1 = new Dimension().withName("abc");
		Dimension d2 = new Dimension().withName("def");
		assertThat(DimensionComparator.INSTANCE.compare(d1, d2), lessThan(0));
	}
	
	@Test
	public void greater() {
		Dimension d1 = new Dimension().withName("def");
		Dimension d2 = new Dimension().withName("abc");
		assertThat(DimensionComparator.INSTANCE.compare(d1, d2), greaterThan(0));
	}
	
	@Test
	public void firstNull() {
		Dimension d = new Dimension().withName("def");
		assertEquals(-1, DimensionComparator.INSTANCE.compare(null, d));
	}
	
	@Test
	public void secondNull() {
		Dimension d = new Dimension().withName("def");
		assertEquals(1, DimensionComparator.INSTANCE.compare(d, null));
	}
	
}