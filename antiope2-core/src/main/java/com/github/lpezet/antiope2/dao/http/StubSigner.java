/**
 * 
 */
package com.github.lpezet.antiope2.dao.http;

import com.github.lpezet.antiope2.APIClientException;
import com.github.lpezet.antiope2.be.IAPICredentials;

/**
 * @author Luc Pezet
 *
 */
public class StubSigner implements Signer {
	@Override
	public void sign(IHttpRequest pRequest, IAPICredentials pCredentials) throws APIClientException {
		// nop
	}

}
