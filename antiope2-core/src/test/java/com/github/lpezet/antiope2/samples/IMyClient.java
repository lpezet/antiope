/**
 * 
 */
package com.github.lpezet.antiope2.samples;

/**
 * @author Luc Pezet
 *
 */
public interface IMyClient {

	public MyResponse ask(MyRequest pRequest) throws Exception;
	
}
