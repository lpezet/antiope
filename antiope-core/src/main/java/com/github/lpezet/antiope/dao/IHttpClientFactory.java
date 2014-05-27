/**
 * 
 */
package com.github.lpezet.antiope.dao;

import org.apache.http.client.HttpClient;

import com.github.lpezet.antiope.be.APIConfiguration;

/**
 * @author luc
 *
 */
public interface IHttpClientFactory {

	public HttpClient createHttpClient(APIConfiguration pConfiguration);
	
}
