/**
 * 
 */
package com.github.lpezet.antiope2.dao;

import com.github.lpezet.antiope2.be.APIConfiguration;

/**
 * @author Luc Pezet
 *
 */
public interface IAPIConfigurationAware {

	public APIConfiguration getAPIConfiguration();
	
	public void setAPIConfiguration(APIConfiguration pAPIConfiguration);
	
}
