/**
 * The MIT License
 * Copyright (c) 2014 Luc Pezet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
