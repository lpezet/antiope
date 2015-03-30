/**
 * 
 */
package com.github.lpezet.antiope2.dao.http;

import com.github.lpezet.antiope2.dao.IMarshaller;

/**
 * @author Luc Pezet
 *
 */
public interface IHttpMarshaller<Source, Target extends IHttpRequest> extends IMarshaller<Source, Target> {

}
