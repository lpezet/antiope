/**
 * 
 */
package com.github.lpezet.antiope2;

import com.github.lpezet.antiope2.dao.http.IHttpNetworkIO;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;


/**
 * @author Luc Pezet
 *
 */
public class MyClient {

	private IHttpNetworkIO<IHttpRequest, IHttpResponse> mNetworkIO;
	private IMetricsCollector mMetricsCollector;
	
	public MyClient(IHttpNetworkIO<IHttpRequest, IHttpResponse> pNetworkIO, IMetricsCollector pMetricsCollector) {
		mNetworkIO = pNetworkIO;
		mMetricsCollector = pMetricsCollector;
	}
	
	public MyResponse ask(MyRequest pRequest) throws Exception {
		
		MyIO<MyRequest, MyResponse> oIO = new MyIO<MyRequest, MyResponse>(new MyRequestMarshaller(), mNetworkIO, new MyResponseUnmarshaller(), mMetricsCollector);
		
		/*
		IHttpRequest oHttpRequest = new MyRequestMarshaller().perform(pRequest);
		oHttpRequest.setExecutionContext(oContext);
		
		IHttpResponse oHttpResponse = mNetworkIO.perform(oHttpRequest);
		return new MyResponseUnmarshaller().perform(oHttpResponse);
		*/
		return oIO.perform(pRequest);
	}
	
}
