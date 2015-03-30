/**
 * 
 */
package com.github.lpezet.antiope2.dao;



/**
 * @author Luc Pezet
 *
 */
public interface IExecutionContextAware {

	public ExecutionContext getExecutionContext();
	
	public void setExecutionContext(ExecutionContext pContext);
	
}
