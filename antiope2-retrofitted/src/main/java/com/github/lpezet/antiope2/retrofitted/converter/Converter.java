
package com.github.lpezet.antiope2.retrofitted.converter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * @author Luc Pezet
 *
 */
public interface Converter {
	
	
	public Object deserialize(InputStream pBody, Type pType) throws IOException;
	
	public InputStream serialize(Object pBody, Type pType);
	
}
