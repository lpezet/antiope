/**
 * 
 */
package com.github.lpezet.antiope.be;


/**
 * @author luc
 *
 */
public class VersionInfo {

	private String mAPIName;
	private String mVersion = null;
    private String mPlatform = null;
    private String mUserAgent = null;
    
    public VersionInfo(String pAPIName, String pVersion, String pPlatform, String pUserAgent) {
    	mAPIName = pAPIName;
		mVersion = pVersion;
		mPlatform = pPlatform;
		mUserAgent = pUserAgent;
	}
    
    public String getAPIName() {
		return mAPIName;
	}

    public String getVersion() {
		return mVersion;
	}
    
    public String getPlatform() {
    	return mPlatform;
    }

    public String getUserAgent() {
    	 return mUserAgent;
    }
}
