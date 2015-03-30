/**
 * 
 */
package com.github.lpezet.antiope2.dao.http.apache;

import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.java.patterns.worker.IWorker;

/**
 * @author Luc Pezet
 *
 */
public interface IApacheHttpClientMarshaller extends IWorker<IHttpRequest, ApacheHttpRequest> {

}
