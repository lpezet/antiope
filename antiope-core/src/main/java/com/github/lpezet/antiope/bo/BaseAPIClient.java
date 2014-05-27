/**
 * 
 */
package com.github.lpezet.antiope.bo;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lpezet.antiope.APIClientException;
import com.github.lpezet.antiope.APIServiceException;
import com.github.lpezet.antiope.be.APIConfiguration;
import com.github.lpezet.antiope.be.APIWebServiceRequest;
import com.github.lpezet.antiope.be.DefaultAPICredentialsProviderChain;
import com.github.lpezet.antiope.be.IAPICredentials;
import com.github.lpezet.antiope.be.IAPICredentialsProvider;
import com.github.lpezet.antiope.be.StaticCredentialsProvider;
import com.github.lpezet.antiope.dao.DefaultErrorResponseHandler;
import com.github.lpezet.antiope.dao.ExecutionContext;
import com.github.lpezet.antiope.dao.HttpResponseHandler;
import com.github.lpezet.antiope.dao.Request;
import com.github.lpezet.antiope.dao.Response;
import com.github.lpezet.antiope.transform.Unmarshaller;

/**
 * @author luc
 *
 */
public abstract class BaseAPIClient<R> extends AbstractClient {
	
	protected final Logger	logger	= LoggerFactory.getLogger(this.getClass());
	
	public BaseAPIClient(APIConfiguration pConfiguration) {
		this(pConfiguration, new DefaultAPICredentialsProviderChain(pConfiguration));
	}

	public BaseAPIClient(APIConfiguration pConfiguration, IAPICredentials pCredentials) {
		this(pConfiguration, new StaticCredentialsProvider(pCredentials));
	}

	public BaseAPIClient(APIConfiguration pConfiguration, IAPICredentialsProvider pCrendentialsProvider) {
		super(pConfiguration);
		setCredentialsProvider(pCrendentialsProvider);
	}

	protected <X, Y extends APIWebServiceRequest> Response<X> invoke(Request<Y> request, Unmarshaller<X, R> unmarshaller, ExecutionContext executionContext) {
			request.setEndpoint(getEndpoint());
			logger.debug("Endpoint = " + getEndpoint());
			APIWebServiceRequest originalRequest = request.getOriginalRequest();
			for (Entry<String, String> entry : originalRequest.copyPrivateRequestParameters().entrySet()) {
				request.addParameter(entry.getKey(), entry.getValue());
			}

			IAPICredentials credentials = getCredentialsProvider().getCredentials();
			if (originalRequest.getCredentials() != null) {
				credentials = originalRequest.getCredentials();
			}

			executionContext.setCredentials(credentials);
			
			DefaultErrorResponseHandler errorResponseHandler = new DefaultErrorResponseHandler(mExceptionUnmarshallers);
			return doInvoke(request, unmarshaller, errorResponseHandler, executionContext);
		}
	
	protected abstract <T> Response<T> doInvoke(Request<?> pRequest, Unmarshaller<T, R> pUnmarshaller, HttpResponseHandler<APIServiceException> pErrorResponseHandler, ExecutionContext pExecutionContext) throws APIClientException, APIServiceException;
	
}
