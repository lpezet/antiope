/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Random;

/**
 * @author luc
 *
 */
public class Distribution<T> {
	
	private static class CircularIterator<T> implements Iterator<T> {
		
		private Object mLock = new Object();
		private Distribution<T> mDist;
		private int mNext = 0;
		
		public CircularIterator(Distribution<T> pDist) {
			mDist = pDist;
		}
		
		@Override
		public boolean hasNext() {
			return mDist.getArray() != null && mDist.getArray().length > 0;
		}
		@Override
		public T next() {
			T oResult = null;
			synchronized (mLock) {
				oResult = mDist.get(mNext);
				mNext++;
				if (mNext >= mDist.size()) mNext = 0;
			}
			return oResult;
		}
		
		public void remove() {
			throw new RuntimeException("Not supported.");
		};
	}
	
	private Random mRandom = new Random();
	private int mSize;
	private int mFilledIn;
	private T[] mArray;
	private T mDefault;
	
	public Distribution(Class<T> pClass, int pSize) {
		this(pClass, null, pSize);
	}
	
	public Distribution(Class<T> pClass, T pDefault, int pSize) {
		mSize = pSize;
		mArray = (T[]) Array.newInstance(pClass, pSize);
		mDefault = pDefault;
	}
	
	public synchronized double fill(T pObject, double pProbability) {
		int oSampleSize = mSize - mFilledIn;
		double oProbabilityWithinRemainingSpots = pProbability * mSize / (double) oSampleSize;
		long oThreshold = Math.round( oSampleSize * (1.0 - oProbabilityWithinRemainingSpots) );
		int oActualIterations = 0;
		int oFilledInCounter = 0;
		for (int i = 0; i < mArray.length; i++) {
			if (mArray[i] != null) continue;
			oActualIterations++;
			if (mRandom.nextInt(oSampleSize) >= oThreshold) {
				oFilledInCounter++;
				mArray[i] = pObject;
			}
		}
		mFilledIn += oFilledInCounter;
		//System.out.println(
		//		">> Sub-fill-in rate = " + (oFilledInCounter / (double) oSampleSize) * 100 +
		//		", Actual fill-in rate = " + (oFilledInCounter / (double) mSize) * 100);
		double oActulFillInRate = (oFilledInCounter / (double) mSize);
		return oActulFillInRate - pProbability;
	}
	
	public Iterator<T> getCircularIterator() {
		return new CircularIterator<T>(this);
	}
	
	protected T[] getArray() {
		return mArray;
	}
	
	public int size() {
		return mSize;
	}
	
	public T get(int pIndex) {
		return mArray[pIndex] == null ? mDefault : mArray[pIndex];
	}
}
