/**
 * 
 */
package com.github.lpezet.antiope2.samples;

import com.github.lpezet.antiope2.dao.http.IHttpNetworkIO;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;
import com.github.lpezet.antiope2.samples.internal.MyIO;
import com.github.lpezet.antiope2.samples.internal.MyRequestMarshaller;
import com.github.lpezet.antiope2.samples.internal.MyResponseUnmarshaller;


/**
 * @author Luc Pezet
 *
 */
public class MyClient implements IMyClient {

	private MyIO<MyRequest, MyResponse> mAskIO;
	
	public MyClient(IHttpNetworkIO<IHttpRequest, IHttpResponse> pNetworkIO, IMetricsCollector pMetricsCollector) {
		setupIOs(pNetworkIO, pMetricsCollector);
	}
	
	private void setupIOs(IHttpNetworkIO<IHttpRequest,IHttpResponse> pNetworkIO, IMetricsCollector pMetricsCollector) {
		mAskIO = new MyIO<MyRequest, MyResponse>(new MyRequestMarshaller(pMetricsCollector), pNetworkIO, new MyResponseUnmarshaller());
	}

	public MyResponse ask(MyRequest pRequest) throws Exception {
		return mAskIO.perform(pRequest);
	}
	
}
