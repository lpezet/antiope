/**
 * 
 */
package com.github.lpezet.antiope;

/**
 * @author luc
 *
 */
public class APIClientException extends RuntimeException {

	private static final long	serialVersionUID	= 3747317146193470085L;

	public APIClientException() {
		super();
	}

	public APIClientException(String pMessage, Throwable pCause) {
		super(pMessage, pCause);
	}

	public APIClientException(String pMessage) {
		super(pMessage);
	}

	public APIClientException(Throwable pCause) {
		super(pCause);
	}

	
}
