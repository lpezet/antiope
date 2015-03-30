/**
 * 
 */
package com.github.lpezet.antiope2;

import java.net.URI;
import java.net.URISyntaxException;

import com.github.lpezet.antiope2.dao.IMarshaller;
import com.github.lpezet.antiope2.dao.http.HttpExecutionContext;
import com.github.lpezet.antiope2.dao.http.HttpMethodName;
import com.github.lpezet.antiope2.dao.http.HttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.metrics.BaseMetrics;
import com.github.lpezet.antiope2.metrics.IMetrics;
import com.github.lpezet.antiope2.metrics.IMetricsCollector;

/**
 * @author Luc Pezet
 *
 */
public class MyRequestMarshaller implements IMarshaller<MyRequest, IHttpRequest> {
	
	private static final URI ENDPOINT = newURI("http://freegeoip.net");
	private IMetricsCollector mMetricsCollector;
	
	public MyRequestMarshaller() {
		this(null);
	}
	
	public MyRequestMarshaller(IMetricsCollector pMetricsCollector) {
		mMetricsCollector = pMetricsCollector;
	}

	@Override
	public IHttpRequest perform(MyRequest pSource) throws Exception {
		IMetrics oMetrics = new BaseMetrics();
		HttpExecutionContext oContext = new HttpExecutionContext(oMetrics, mMetricsCollector);
		
		HttpRequest oResult = new HttpRequest("MyService");
		oResult.setExecutionContext(oContext);
		oResult.setEndpoint(ENDPOINT);
		oResult.setResourcePath("/json/" + pSource.getIP());
		oResult.setHttpMethod(HttpMethodName.GET);
		//oResult.addHeader("toto", "titi");
		return oResult;
	}

	private static URI newURI(String pURI) {
		try {
			return new URI(pURI);
		} catch (URISyntaxException e) {
			return null;
		}
	}

}
