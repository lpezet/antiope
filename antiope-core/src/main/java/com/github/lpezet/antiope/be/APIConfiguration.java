/**
 * 
 */
package com.github.lpezet.antiope.be;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author luc
 */
public class APIConfiguration {

	private boolean						mProfilingEnabled;
	
	/**
	 * globalTimeOffset is a time offset that is used to globally adjust the
	 * client clock skew. Java SDK already provides timeOffset and accessor methods
	 * in {@link Request} class but those are used per request, whereas
	 * this variable will adjust clock skew globally. Java SDK detects clock
	 * skew errors and adjusts global clock skew automatically.
	 */
	private final AtomicInteger	globalTimeOffset	= new AtomicInteger(0);
	
	 /** The default timeout for a connected socket. */
    public static final int DEFAULT_SOCKET_TIMEOUT = 50 * 1000;

    /** The default max connection pool size. */
    public static final int DEFAULT_MAX_CONNECTIONS = 50;

    /** The default HTTP user agent header for TSG Java SDK clients. */
    public static final String DEFAULT_USER_AGENT = "";// VersionInfoUtils.getUserAgent();

    /**
     * Default request retry policy, including the maximum retry count of 3, the
     * default retry condition and the default back-off strategy.
     * <p>
     * Note this default policy might be overridden by a service-specific
     * default policy, if the user doesn't provide a custom RetryPolicy
     * implementation by {@link #setRetryPolicy(RetryPolicy)}. For example,
     * AmazonDynamoDBClient by default uses a different retry policy
     * {@link PredefinedRetryPolicies#DYNAMODB_DEFAULT}.
     * 
     * @see PredefinedRetryPolicies#DEFAULT
     * @see PredefinedRetryPolicies#DYNAMODB_DEFAULT
     */
    //public static final RetryPolicy DEFAULT_RETRY_POLICY = PredefinedRetryPolicies.DEFAULT;
    
    /**
     * The default on whether to use the {@link IdleConnectionReaper} to manage stale connections
     *
     * @see IdleConnectionReaper
     */
    public static final boolean DEFAULT_USE_REAPER = true;
    
    public static final String DEFAULT_ENV_VAR_ACCESS_KEY = "API_ACCESS_KEY";
    public static final String DEFAULT_ENV_VAR_SECRET_KEY = "API_SECRET_KEY";
    
    public static final String DEFAULT_SYS_PROP_ACCESS_KEY = "api.accessKey";
    public static final String DEFAULT_SYS_PROP_SECRET_KEY = "api.secretKey";
    
    private String mEnvVarAccessKey = DEFAULT_ENV_VAR_ACCESS_KEY;
    private String mEnvVarSecretKey = DEFAULT_ENV_VAR_SECRET_KEY;
    
    private String mSysPropAccessKey = DEFAULT_SYS_PROP_ACCESS_KEY;
    private String mSysPropSecretKey = DEFAULT_SYS_PROP_SECRET_KEY;
    

    /** The HTTP user agent header passed with all HTTP requests. */
    private String mUserAgent = DEFAULT_USER_AGENT;

    /**
     * The maximum number of times that a retryable failed request (ex: a 5xx
     * response from a service) will be retried. Or -1 if the user has not
     * explicitly set this value, in which case the configured RetryPolicy will
     * be used to control the retry count.
     */
    private int mMaxErrorRetry = -1;
    
    /** The retry policy upon failed requests. **/
    //private RetryPolicy retryPolicy = DEFAULT_RETRY_POLICY;

    private boolean mCheckSSLCertificates = true;
    
    /**
     * The protocol to use when connecting to Amazon Web Services.
     * <p>
     * The default configuration is to use HTTPS for all requests for increased
     * security.
     */
    private boolean mSecure = true;

    /** Optionally specifies the proxy host to connect through. */
    private String mProxyHost = null;

    /** Optionally specifies the port on the proxy host to connect through. */
    private int mProxyPort = -1;

    /** Optionally specifies the user name to use when connecting through a proxy. */
    private String mProxyUsername = null;

    /** Optionally specifies the password to use when connecting through a proxy. */
    private String mProxyPassword = null;

    /** Optional Windows domain name for configuring NTLM proxy support. */
    private String mProxyDomain = null;

    /** Optional Windows workstation name for configuring NTLM proxy support. */
    private String mProxyWorkstation = null;

    /** The maximum number of open HTTP connections. */
    private int mMaxConnections = DEFAULT_MAX_CONNECTIONS;

    /**
     * The amount of time to wait (in milliseconds) for data to be transfered
     * over an established, open connection before the connection is timed out.
     * A value of 0 means infinity, and is not recommended.
     */
    private int mSocketTimeout = DEFAULT_SOCKET_TIMEOUT;

    /**
     * The amount of time to wait (in milliseconds) when initially establishing
     * a connection before giving up and timing out. A value of 0 means
     * infinity, and is not recommended.
     */
    private int mConnectionTimeout = 50 * 1000;

    /**
     * Optional size hint (in bytes) for the low level TCP send buffer. This is
     * an advanced option for advanced users who want to tune low level TCP
     * parameters to try and squeeze out more performance.
     */
    private int mSocketSendBufferSizeHint = 0;

    /**
     * Optional size hint (in bytes) for the low level TCP receive buffer. This
     * is an advanced option for advanced users who want to tune low level TCP
     * parameters to try and squeeze out more performance.
     */
    private int mSocketReceiveBufferSizeHint = 0;

    /**
     * Optional whether to use the {@link IdleConnectionReaper} to manage stale connections. A reason for not running
     * the {@link IdleConnectionReaper} can be if running in an environment where the modifyThread and modifyThreadGroup
     * permissions are not allowed.
     */
    private boolean mUseReaper = DEFAULT_USE_REAPER;
    
    public int getConnectionTimeout() {
		return mConnectionTimeout;
	}
    
    public void setConnectionTimeout(int pConnectionTimeout) {
		mConnectionTimeout = pConnectionTimeout;
	}
    
    public int getMaxConnections() {
		return mMaxConnections;
	}
    
    public void setMaxConnections(int pMaxConnections) {
		mMaxConnections = pMaxConnections;
	}
    
    public int getMaxErrorRetry() {
		return mMaxErrorRetry;
	}
    
    public void setMaxErrorRetry(int pMaxErrorRetry) {
		mMaxErrorRetry = pMaxErrorRetry;
	}
    
    public String getProxyDomain() {
		return mProxyDomain;
	}
    
    public void setProxyDomain(String pProxyDomain) {
		mProxyDomain = pProxyDomain;
	}
    
    public String getProxyHost() {
		return mProxyHost;
	}
    
    public void setProxyHost(String pProxyHost) {
		mProxyHost = pProxyHost;
	}
    
    public String getProxyPassword() {
		return mProxyPassword;
	}
    
    public void setProxyPassword(String pProxyPassword) {
		mProxyPassword = pProxyPassword;
	}
    
    public int getProxyPort() {
		return mProxyPort;
	}
    
    public void setProxyPort(int pProxyPort) {
		mProxyPort = pProxyPort;
	}
    
    public String getProxyUsername() {
		return mProxyUsername;
	}
    
    public void setProxyUsername(String pProxyUsername) {
		mProxyUsername = pProxyUsername;
	}
    
    public String getProxyWorkstation() {
		return mProxyWorkstation;
	}
    
    public void setProxyWorkstation(String pProxyWorkstation) {
		mProxyWorkstation = pProxyWorkstation;
	}
    
    public int getSocketReceiveBufferSizeHint() {
		return mSocketReceiveBufferSizeHint;
	}
    
    public void setSocketReceiveBufferSizeHint(int pSocketReceiveBufferSizeHint) {
		mSocketReceiveBufferSizeHint = pSocketReceiveBufferSizeHint;
	}
    
    public int getSocketSendBufferSizeHint() {
		return mSocketSendBufferSizeHint;
	}
    
    public void setSocketSendBufferSizeHint(int pSocketSendBufferSizeHint) {
		mSocketSendBufferSizeHint = pSocketSendBufferSizeHint;
	}
    
    public int getSocketTimeout() {
		return mSocketTimeout;
	}
    
    public void setSocketTimeout(int pSocketTimeout) {
		mSocketTimeout = pSocketTimeout;
	}
    
    public String getUserAgent() {
		return mUserAgent;
	}
    
    public void setUserAgent(String pUserAgent) {
		mUserAgent = pUserAgent;
	}
    
    public boolean isUseReaper() {
		return mUseReaper;
	}
    
    public void setUseReaper(boolean pUseReaper) {
		mUseReaper = pUseReaper;
	}
    
    public boolean isCheckSSLCertificates() {
		return mCheckSSLCertificates;
	}
    
    public void setCheckSLLCertificates(boolean pCheckSSLCertificates) {
		mCheckSSLCertificates = pCheckSSLCertificates;
    }

    public void setEnvVarAccessKey(String pEnvVarAccessKey) {
		mEnvVarAccessKey = pEnvVarAccessKey;
	}
    
    public String getEnvVarAccessKey() {
		return mEnvVarAccessKey;
	}
    
    public void setEnvVarSecretKey(String pEnvVarSecretKey) {
		mEnvVarSecretKey = pEnvVarSecretKey;
	}
    
    public String getEnvVarSecretKey() {
		return mEnvVarSecretKey;
	}
    
    public String getSysPropAccessKey() {
		return mSysPropAccessKey;
	}
    
    public void setSysPropAccessKey(String pSysPropAccessKey) {
		mSysPropAccessKey = pSysPropAccessKey;
	}
    
    public String getSysPropSecretKey() {
		return mSysPropSecretKey;
	}
    
    public void setSysPropSecretKey(String pSysPropSecretKey) {
		mSysPropSecretKey = pSysPropSecretKey;
	}
    
    public boolean isSecure() {
		return mSecure;
	}
    
	/**
	 * Sets the global time offset. If this value is set then all the subsequent
	 * requests will use this value to generate timestamps. To adjust clock skew
	 * per request use {@link Request#setTimeOffset(int)}
	 * 
	 * @param timeOffset
	 *            the time difference between local client and server
	 */
	public void setGlobalTimeOffset(int timeOffset) {
		globalTimeOffset.set(timeOffset);
	}

	/**
	 * Gets the global time offset. See {@link Request#getTimeOffset()} if global time
	 * offset is not set.
	 * 
	 * @return globalTimeOffset an AtomicInteger that holds the value of time
	 *         offset
	 */
	public int getGlobalTimeOffset() {
		return globalTimeOffset.get();
	}
	
	public void setProfilingEnabled(boolean pProfilingEnabled) {
		mProfilingEnabled = pProfilingEnabled;
	}

	public boolean isProfilingEnabled() {
		return mProfilingEnabled;
	}
}
