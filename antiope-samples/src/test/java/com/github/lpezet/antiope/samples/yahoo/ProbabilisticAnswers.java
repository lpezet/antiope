/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import java.util.Iterator;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author luc
 *
 */
public class ProbabilisticAnswers<T> implements Answer<T> {
	
	private Distribution<Answer> mDistribution;
	private Iterator<Answer> mIterator;
	private Answer<T> mCurrentAnswer;
	
	public ProbabilisticAnswers(int pSampleSize) {
		this(pSampleSize, null);
	}
	
	public ProbabilisticAnswers(int pSampleSize, Answer<T> pDefault) {
		mDistribution = new Distribution<Answer>(Answer.class, pDefault, pSampleSize);
		mIterator = mDistribution.getCircularIterator();
	}
	
	public ProbabilisticAnswers<T> answerWith(Answer<T> pAnswer, double pProbability) {
		mDistribution.fill(pAnswer, pProbability);
		return this;
	}
	

	@Override
	public T answer(InvocationOnMock pInvocation) throws Throwable {
		Answer a = mIterator.next();
		mCurrentAnswer = a;
		return (T) a.answer(pInvocation);
	}
	
	public Answer<T> getCurrentAnswer() {
		return mCurrentAnswer;
	}

}
