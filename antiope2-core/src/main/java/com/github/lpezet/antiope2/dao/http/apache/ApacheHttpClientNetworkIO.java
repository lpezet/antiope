/**
 * The MIT License
 * Copyright (c) 2014 Luc Pezet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.lpezet.antiope2.dao.http.apache;

import org.apache.http.client.HttpClient;

import com.github.lpezet.antiope2.dao.INetworkIO;
import com.github.lpezet.antiope2.dao.http.IHttpNetworkIO;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpResponse;

/**
 * @author Luc Pezet
 *
 */
public class ApacheHttpClientNetworkIO  implements IHttpNetworkIO<IHttpRequest, IHttpResponse> {

	private IApacheHttpClientMarshaller mApacheMarshaller;
	private IApacheHttpClientUnmarshaller mApacheUnmarshaller;
	private INetworkIO<ApacheHttpRequest, ApacheHttpResponse> mApacheNIO;
	
	public ApacheHttpClientNetworkIO(HttpClient pHttpClient) {
		this(new ApacheHttpClientMarshaller(), new ApacheHttpClient(pHttpClient), new ApacheHttpClientUnmarshaller());
	}
	
	public ApacheHttpClientNetworkIO(IApacheHttpClientMarshaller pMarshaller, INetworkIO<ApacheHttpRequest, ApacheHttpResponse> pNIO, IApacheHttpClientUnmarshaller pUnmarshaller) {
		mApacheMarshaller = pMarshaller;
		mApacheNIO = pNIO;
		mApacheUnmarshaller = pUnmarshaller;
	}
	
	@Override
	public IHttpResponse perform(IHttpRequest pRequest) throws Exception {
		ApacheHttpRequest oRQ = mApacheMarshaller.perform(pRequest);
		ApacheHttpResponse oRS = mApacheNIO.perform(oRQ);
		IHttpResponse oResult = mApacheUnmarshaller.perform(oRS);
		return oResult;
	}
}
