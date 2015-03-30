/**
 * 
 */
package com.github.lpezet.antiope2.dao.http;

import com.github.lpezet.antiope2.dao.INetworkIO;

/**
 * @author Luc Pezet
 *
 */
public interface IHttpNetworkIO<RQ extends IHttpRequest, RS extends IHttpResponse> extends INetworkIO<RQ, RS> {

}
