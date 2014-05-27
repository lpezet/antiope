/**
 * 
 */
package com.github.lpezet.antiope.be;


/**
 * @author luc
 *
 */
public class BasicAPICredentials implements IAPICredentials {

	private String mAccessKey;
	private String mSecretKey;
	
	public BasicAPICredentials(String pAccessKey, String pSecretKey) {
		mAccessKey = pAccessKey;
		mSecretKey = pSecretKey;
	}

	public String getAccessKey() {
		return mAccessKey;
	}

	public void setAccessKey(String pAccessKey) {
		mAccessKey = pAccessKey;
	}

	public String getSecretKey() {
		return mSecretKey;
	}

	public void setSecretKey(String pSecretKey) {
		mSecretKey = pSecretKey;
	}

}
