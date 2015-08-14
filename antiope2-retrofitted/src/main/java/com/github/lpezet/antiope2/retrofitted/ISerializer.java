/**
 * 
 */
package com.github.lpezet.antiope2.retrofitted;

import java.io.InputStream;

import com.github.lpezet.java.patterns.worker.IWorker;

/**
 * @author Luc Pezet
 *
 */
public interface ISerializer<W> extends IWorker<W, InputStream> {

}
