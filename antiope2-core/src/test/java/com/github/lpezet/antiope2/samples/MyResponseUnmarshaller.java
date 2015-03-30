/**
 * 
 */
package com.github.lpezet.antiope2.samples;

import org.apache.commons.io.IOUtils;

import com.github.lpezet.antiope2.dao.IUnmarshaller;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;

/**
 * @author Luc Pezet
 *
 */
public class MyResponseUnmarshaller implements IUnmarshaller<IHttpResponse, MyResponse> {

	@Override
	public MyResponse perform(IHttpResponse pSource) throws Exception {
		String oJSON = pSource.getContent() != null ? IOUtils.toString(pSource.getContent(), "UTF8") : "{}";
		MyResponse oResponse = new MyResponse();
		oResponse.setContent(oJSON);
		return oResponse;
	}
}
