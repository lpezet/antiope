/**
 * 
 */
package com.github.lpezet.antiope2.retrofitted.converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Luc Pezet
 *
 */
public class JacksonConverter implements Converter {
	
	private ObjectMapper mObjectMapper;
	
	public JacksonConverter() {
		this( new ObjectMapper() );
	}
	
	public JacksonConverter(ObjectMapper pObjectMapper) {
		mObjectMapper = pObjectMapper;
	}
	
	
	@Override
	public Object deserialize(InputStream pBody, Type pType) throws IOException {
		JavaType oJType = mObjectMapper.constructType( pType );
		return mObjectMapper.readValue(pBody, oJType);
	}
	
	@Override
	public InputStream serialize(Object pBody, Type pType) {
		try {
			return new ByteArrayInputStream( mObjectMapper.writeValueAsBytes( pBody ) );
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
