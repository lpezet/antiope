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
package com.github.lpezet.antiope2;



/**
 * @author luc
 *
 */
public class APIServiceException extends APIClientException {
	
	private static final long	serialVersionUID	= -408213717238880974L;

	/**
     * Indicates who is responsible (if known) for a failed request.
     *
     * <p>For example, if a client is using an invalid access key,
     * the returned exception will indicate that there is an error in the
     * request the caller is sending. Retrying that same request will *not*
     * result in a successful response. The Client ErrorType indicates that
     * there is a problem in the request the user is sending (ex: incorrect
     * access keys, invalid parameter value, missing parameter, etc.), and that
     * the caller must take some action to correct the request before it should
     * be resent. Client errors are typically associated an HTTP error code in
     * the 4xx range.
     *
     * <p>The Service ErrorType indicates that although the request the
     * caller sent was valid, the service was unable to fulfill the request
     * because of problems on the service's side. These types of errors can be
     * retried by the caller since the caller's request was valid and the
     * problem occurred while processing the request on the service side.
     * Service errors will be accompanied by an HTTP error code in the 5xx
     * range.
     *
     * <p>Finally, if there isn't enough information to determine who's
     * fault the error response is, an Unknown ErrorType will be set.
     */
    public enum ErrorType {
        Client,
        Service,
        Unknown
    }

    /**
     * The unique API identifier for the service request the caller made. The
     * API request ID can uniquely identify the API request, and is used for
     * reporting an error to API support team.
     */
    private String requestId;

    /**
     * The API error code represented by this exception (ex:
     * InvalidParameterValue).
     */
    private String errorCode;

    /**
     * Indicates (if known) whether this exception was the fault of the caller
     * or the service.
     *
     * @see ErrorType
     */
    private ErrorType errorType = ErrorType.Unknown;

    /** The HTTP status code that was returned with this error */
    private int statusCode;

    /**
     * The name of the Amazon service that sent this error response.
     */
    private String serviceName;

    /**
     * Constructs a new AmazonServiceException with the specified message.
     *
     * @param message
     *            An error message describing what went wrong.
     */
    public APIServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new AmazonServiceException with the specified message and
     * exception indicating the root cause.
     *
     * @param message
     *            An error message describing what went wrong.
     * @param cause
     *            The root exception that caused this exception to be thrown.
     */
    public APIServiceException(String message, Exception cause) {
        super(message, cause);
    }

    /**
     * Sets the API requestId for this exception.
     *
     * @param requestId
     *            The unique identifier for the service request the caller made.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Returns the API request ID that uniquely identifies the service request
     * the caller made.
     *
     * @return The API request ID that uniquely identifies the service request
     *         the caller made.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the name of the service that sent this error response.
     *
     * @param serviceName
     *            The name of the service that sent this error response.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Returns the name of the service that sent this error response.
     *
     * @return The name of the service that sent this error response.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the API error code represented by this exception.
     *
     * @param errorCode
     *            The API error code represented by this exception.
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Returns the API error code represented by this exception.
     *
     * @return The API error code represented by this exception.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the type of error represented by this exception (sender, receiver,
     * or unknown), indicating if this exception was the caller's fault, or the
     * service's fault.
     *
     * @param errorType
     *            The type of error represented by this exception (sender or
     *            receiver), indicating if this exception was the caller's fault
     *            or the service's fault.
     */
    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    /**
     * Indicates who is responsible for this exception (caller, service,
     * or unknown).
     *
     * @return A value indicating who is responsible for this exception (caller, service, or unknown).
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Sets the HTTP status code that was returned with this service exception.
     *
     * @param statusCode
     *            The HTTP status code that was returned with this service
     *            exception.
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code that was returned with this service
     * exception.
     *
     * @return The HTTP status code that was returned with this service
     *         exception.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return "Status Code: " + getStatusCode() + ", "
            + "API Service: " + getServiceName() + ", "
            + "API Request ID: " + getRequestId() + ", "
            + "API Error Code: " + getErrorCode() + ", "
            + "API Error Message: " + super.getMessage();
    }

}
