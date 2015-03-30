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
	
	private MyIO<MyRequest, MyResponse> mAskIO;
	private IMetricsCollector mMetricsCollector;
	
	public MyClient(IHttpNetworkIO<IHttpRequest, IHttpResponse> pNetworkIO, IMetricsCollector pMetricsCollector) {
		mNetworkIO = pNetworkIO;
		mMetricsCollector = pMetricsCollector;
		setupIOs();
	}
	
	private void setupIOs() {
		mAskIO = new MyIO<MyRequest, MyResponse>(new MyRequestMarshaller(mMetricsCollector), mNetworkIO, new MyResponseUnmarshaller());
	}

	public MyResponse ask(MyRequest pRequest) throws Exception {
		return mAskIO.perform(pRequest);
	}
	
}
