package com.github.lpezet.antiope.metrics.aws;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.github.lpezet.antiope.be.APIWebServiceResponse;
import com.github.lpezet.antiope.dao.HttpResponse;
import com.github.lpezet.antiope.dao.HttpResponseHandler;

class SimpleResponseHandler implements HttpResponseHandler<APIWebServiceResponse<String>> {

	@Override
	public APIWebServiceResponse<String> handle(HttpResponse pResponse) throws Exception {
		InputStream oContent = pResponse.getContent();
		String oBody = IOUtils.toString(oContent);
		APIWebServiceResponse<String> oResponse = new APIWebServiceResponse<String>();
    	oResponse.setResult(oBody);
    	return oResponse;
	}

	@Override
	public boolean needsConnectionLeftOpen() {
		return false;
	}
	
}