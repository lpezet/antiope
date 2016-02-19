/**
 * 
 */
package com.github.lpezet.antiope2.retrofitted;

/**
 * @author Luc Pezet
 *
 */
public interface RequestInterceptor {

	
	public void intercept(RequestFacade pRequest);
	
	
	
	public final RequestInterceptor NOP_INTERCEPTOR = new RequestInterceptor() {
		@Override
		public void intercept(RequestFacade pRequest) {
		}
	};
}
