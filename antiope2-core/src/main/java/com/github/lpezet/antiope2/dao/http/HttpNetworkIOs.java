/**
 * 
 */
package com.github.lpezet.antiope2.dao.http;


/**
 * @author Luc Pezet
 *
 */
public final class HttpNetworkIOs {

	public static IHttpNetworkIO<IHttpRequest, IHttpResponse> nonClosingIO(final IHttpNetworkIO<IHttpRequest, IHttpResponse> pImpl) {
		return new AbstractHttpNetworkIO<IHttpRequest, IHttpResponse>() {
			@Override
			protected IHttpResponse doPerform(IHttpRequest pRequest) throws Exception {
				return pImpl.perform(pRequest);
			}
			protected void postPerform(IHttpRequest pRequest, IHttpResponse pResponse) {};
		};
	}
	
	public static IHttpNetworkIO<IHttpRequest, IHttpResponse> closingIO(final IHttpNetworkIO<IHttpRequest, IHttpResponse> pImpl) {
		return new AbstractHttpNetworkIO<IHttpRequest, IHttpResponse>() {
			@Override
			protected IHttpResponse doPerform(IHttpRequest pRequest) throws Exception {
				return pImpl.perform(pRequest);
			}
			@Override
			protected void postPerform(IHttpRequest pRequest, IHttpResponse pResponse) {
				pResponse.close();
			}
		};
	}
}
