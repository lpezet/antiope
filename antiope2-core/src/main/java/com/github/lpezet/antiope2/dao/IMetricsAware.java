/**
 * 
 */
package com.github.lpezet.antiope2.dao;

import com.github.lpezet.antiope2.metrics.IMetrics;

/**
 * @author Luc Pezet
 *
 */
public interface IMetricsAware {

    public IMetrics getMetrics();
        
    public void setMetrics(IMetrics pMetrics);
    
    
}
