/**
 * 
 */
package com.github.lpezet.antiope2.be;

/**
 * @author Luc Pezet
 *
 */
public interface IAPIConfiguration {


    public int getConnectionTimeout() ;
    
    public int getMaxConnections() ;
    
    public int getMaxErrorRetry() ;
    
    public String getProxyDomain() ;
    
    public String getProxyHost() ;
    
    public String getProxyPassword() ;
    
    public int getProxyPort() ;
    
    public String getProxyUsername() ;
    
    public String getProxyWorkstation() ;
    
    public int getSocketReceiveBufferSizeHint() ;
    
    public int getSocketSendBufferSizeHint() ;
    
    public int getSocketTimeout() ;
    
    public String getUserAgent() ;
    
    public boolean isUseReaper() ;
    
    public boolean isCheckSSLCertificates() ;
    
    public String getEnvVarAccessKey() ;
    
    public String getEnvVarSecretKey() ;
    
    public String getSysPropAccessKey() ;
    
    public String getSysPropSecretKey() ;
    
    public boolean isSecure() ;
    
	/**
	 * Sets the global time offset. If this value is set then all the subsequent
	 * requests will use this value to generate timestamps. To adjust clock skew
	 * per request use ;
	 * 
	 * @param timeOffset
	 *            the time difference between local client and server
	 */
	public void setGlobalTimeOffset(int timeOffset) ;

	/**
	 * Gets the global time offset. See ; if global time
	 * offset is not set.
	 * 
	 * @return globalTimeOffset an AtomicInteger that holds the value of time
	 *         offset
	 */
	public int getGlobalTimeOffset() ;

	public boolean isProfilingEnabled();
	
}
