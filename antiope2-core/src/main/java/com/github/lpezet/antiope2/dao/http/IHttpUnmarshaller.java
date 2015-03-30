/**
 * 
 */
package com.github.lpezet.antiope2.dao.http;

import com.github.lpezet.antiope2.dao.IUnmarshaller;

/**
 * @author Luc Pezet
 *
 */
public interface IHttpUnmarshaller<Source extends IHttpResponse, Target> extends IUnmarshaller<Source, Target> {

}
