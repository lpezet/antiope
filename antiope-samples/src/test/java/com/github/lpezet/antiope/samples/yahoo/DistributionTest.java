/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author luc
 *
 */
public class DistributionTest {

	
	@Test
	public void doIt() throws Exception {
		Distribution<String> oArray = new Distribution<String>(String.class, 512);
		
		double oADivergence = oArray.fill("A", 0.25);
		double oBDivergence = oArray.fill("B", 0.10);
		double oCDivergence = oArray.fill("C", 0.5);
		
		int[] oCounters = new int[oArray.size()];
		for (int i = 0; i < oArray.size(); i++) {
			String v = oArray.get(i);
			if ("A".equals(v)) oCounters[0]++;
			else if ("B".equals(v)) oCounters[1]++;
			else if ("C".equals(v)) oCounters[2]++;
		}
		
		double[] oActual = new double[3];
		oActual[0] = (oCounters[0] / (double) oArray.size());
		oActual[1] = (oCounters[1] / (double) oArray.size());
		oActual[2] = (oCounters[2] / (double) oArray.size());
		/*
		System.out.println("########## Probs:");
		System.out.println(
				"A = " +  oActual[0]* 100 + " (off by " + oADivergence*100 + ")"
				+ ", B = " + oActual[1] * 100 + " (off by " + oBDivergence*100 + ")"
				+ ", C = " + oActual[2] * 100 + " (off by " + oCDivergence*100 + ")");
		*/
		assertWithin(oActual[0], 0.25, 2.0);
		assertWithin(oActual[1], 0.10, 3.0);
		assertWithin(oActual[2], 0.5, 1.0);
	}

	private void assertWithin(double pActual, double pExpected, double pDelta) {
		Assert.assertTrue(Math.abs( pActual - pExpected ) <= pDelta);
	}
}
