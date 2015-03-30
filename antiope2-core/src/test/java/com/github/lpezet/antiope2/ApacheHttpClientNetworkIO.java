/**
 * 
 */
package com.github.lpezet.antiope2;

import com.github.lpezet.antiope2.dao.INetworkIO;
import com.github.lpezet.antiope2.dao.http.IHttpNetworkIO;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;
import com.github.lpezet.antiope2.dao.http.apache.ApacheHttpRequest;
import com.github.lpezet.antiope2.dao.http.apache.ApacheHttpResponse;
import com.github.lpezet.antiope2.dao.http.apache.IApacheHttpClientMarshaller;
import com.github.lpezet.antiope2.dao.http.apache.IApacheHttpClientUnmarshaller;

/**
 * @author Luc Pezet
 *
 */
public class ApacheHttpClientNetworkIO implements IHttpNetworkIO<IHttpRequest, IHttpResponse> {

	private IApacheHttpClientMarshaller mApacheMarshaller;
	private IApacheHttpClientUnmarshaller mApacheUnmarshaller;
	private INetworkIO<ApacheHttpRequest, ApacheHttpResponse> mApacheNIO;
	
	public ApacheHttpClientNetworkIO(IApacheHttpClientMarshaller pMarshaller, INetworkIO<ApacheHttpRequest, ApacheHttpResponse> pNIO, IApacheHttpClientUnmarshaller pUnmarshaller) {
		mApacheMarshaller = pMarshaller;
		mApacheNIO = pNIO;
		mApacheUnmarshaller = pUnmarshaller;
	}
	
	@Override
	public IHttpResponse perform(IHttpRequest pRequest) throws Exception {
		ApacheHttpRequest oRQ = mApacheMarshaller.perform(pRequest);
		ApacheHttpResponse oRS = mApacheNIO.perform(oRQ);
		IHttpResponse oResult = mApacheUnmarshaller.perform(oRS);
		return oResult;
	}
}
