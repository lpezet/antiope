/**
 * 
 */
package com.github.lpezet.antiope.dao;

import com.github.lpezet.antiope.APIClientException;
import com.github.lpezet.antiope.be.IAPICredentials;

/**
 * @author Luc Pezet
 *
 */
public class StubSigner implements Signer {

	@Override
	public void sign(Request<?> pRequest, IAPICredentials pCredentials) throws APIClientException {
		// nop
	}

}
