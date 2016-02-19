/**
 * 
 */
package com.github.lpezet.antiope2.retrofitted;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lpezet.antiope2.dao.ExecutionContext;
import com.github.lpezet.antiope2.dao.http.BasicNameValuePair;
import com.github.lpezet.antiope2.dao.http.HttpRequest;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.NameValuePair;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Body;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Field;
import com.github.lpezet.antiope2.retrofitted.annotation.http.FieldMap;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Header;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Path;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Query;
import com.github.lpezet.antiope2.retrofitted.annotation.http.QueryMap;
import com.github.lpezet.antiope2.retrofitted.converter.Converter;
import com.github.lpezet.antiope2.util.StringUtils;

/**
 * @author Luc Pezet
 *
 */
public class RequestBuilder implements RequestFacade {
	
	private Logger mLogger = LoggerFactory.getLogger(this.getClass());
	private MethodInfo mMethodInfo;
	private String mResourcePath;
	private List<NameValuePair> mParameters = new ArrayList<NameValuePair>();
	private List<NameValuePair> mQueryParameters = new ArrayList<NameValuePair>();
	private InputStream mRequestBody;
	private Converter mConverter;
	private String mEndpointUri;
	
	private ExecutionContext mExecutionContext;
	
	private com.github.lpezet.antiope2.dao.http.Headers mHeaders = new com.github.lpezet.antiope2.dao.http.Headers();
	
	public RequestBuilder(String pEndpointUri, MethodInfo pMethodInfo, Converter pDefaultConverter) {
		this(pEndpointUri, pMethodInfo, pDefaultConverter, null);
	}
	
	public RequestBuilder(String pEndpointUri, MethodInfo pMethodInfo, Converter pDefaultConverter, ExecutionContext pExecutionContext) {
		mEndpointUri = pEndpointUri;
		mMethodInfo = pMethodInfo;
		mResourcePath= pMethodInfo.getResourcePath();
		mConverter = pMethodInfo.getConverter() == null ? pDefaultConverter : pMethodInfo.getConverter();
		mHeaders.addAll( pMethodInfo.getHeaders() );
		mExecutionContext = pExecutionContext;
	}
	
	public void setArguments(Object[] pArgs) {
		if (pArgs == null) {
			return;
		}
		int oCount = pArgs.length;
		if (mMethodInfo.isAsync()) {
			// Last arg is Callback
			oCount -= 1;
		}
		for (int i = 0; i < oCount; i++) {
			Object oValue = pArgs[i];

			Annotation oAnnotation = mMethodInfo.getParamAnnotations()[i];
			Class<? extends Annotation> oAnnotationType = oAnnotation.annotationType();
			if (oAnnotationType == Path.class) {
				Path oPath = (Path) oAnnotation;
				String oName = oPath.value();
				if (oValue == null) {
					throw new IllegalArgumentException(
							"Path parameter \"" + oName + "\" value must not be null.");
				}
				addPathParam(oName, oValue.toString(), oPath.encode());
			} else if (oAnnotationType == Query.class) {
				if (oValue != null) { // Skip null values.
					Query query = (Query) oAnnotation;
					addQueryParam(query.value(), oValue, query.template(), query.encodeName(), query.encodeValue());
				}
			} else if (oAnnotationType == QueryMap.class) {
				if (oValue != null) { // Skip null values.
					QueryMap queryMap = (QueryMap) oAnnotation;
					addQueryParamMap(i, (Map<?, ?>) oValue, queryMap.encodeNames(), queryMap.encodeValues());
				}
			} else if (oAnnotationType == Header.class) {
				if (oValue != null) { // Skip null values.
					String name = ((Header) oAnnotation).value();
					if (oValue instanceof Iterable) {
						for (Object iterableValue : (Iterable<?>) oValue) {
							if (iterableValue != null) { // Skip null values.
								addHeader(name, iterableValue.toString());
							}
						}
					} else if (oValue.getClass().isArray()) {
						for (int x = 0, arrayLength = Array.getLength(oValue); x < arrayLength; x++) {
							Object arrayValue = Array.get(oValue, x);
							if (arrayValue != null) { // Skip null values.
								addHeader(name, arrayValue.toString());
							}
						}
					} else {
						addHeader(name, oValue.toString());
					}
				}
			} else if (oAnnotationType == Field.class) {
				if (oValue != null) { // Skip null values.
					Field field = (Field) oAnnotation;
					String name = field.value();
					boolean encode = field.encode();
					if (oValue instanceof Iterable) {
						for (Object iterableValue : (Iterable<?>) oValue) {
							if (iterableValue != null) { // Skip null values.
								addFormField(name, iterableValue.toString(), encode);
							}
						}
					} else if (oValue.getClass().isArray()) {
						for (int x = 0, arrayLength = Array.getLength(oValue); x < arrayLength; x++) {
							Object arrayValue = Array.get(oValue, x);
							if (arrayValue != null) { // Skip null values.
								addFormField(name, arrayValue.toString(), encode);
							}
						}
					} else {
						addFormField(name, oValue.toString(), encode);
					}
				}
			} else if (oAnnotationType == FieldMap.class) {
				if (oValue != null) { // Skip null values.
					FieldMap fieldMap = (FieldMap) oAnnotation;
					boolean encode = fieldMap.encode();
					for (Map.Entry<?, ?> entry : ((Map<?, ?>) oValue).entrySet()) {
						Object entryKey = entry.getKey();
						if (entryKey == null) {
							throw new IllegalArgumentException(
									"Parameter #" + (i + 1) + " field map contained null key.");
						}
						Object entryValue = entry.getValue();
						if (entryValue != null) { // Skip null values.
							addFormField(entryKey.toString(), entryValue.toString(), encode);
						}
					}
				}
			} 
			// TODO: Support for multi part
			/*
			else if (annotationType == Part.class) {
				if (value != null) { // Skip null values.
					String name = ((Part) annotation).value();
					String transferEncoding = ((Part) annotation).encoding();
					Headers headers = Headers.of(
							"Content-Disposition", "name=\"" + name + "\"",
							"Content-Transfer-Encoding", transferEncoding);
					if (value instanceof RequestBody) {
						multipartBuilder.addPart(headers, (RequestBody) value);
					} else if (value instanceof String) {
						multipartBuilder.addPart(headers,
								RequestBody.create(MediaType.parse("text/plain"), (String) value));
					} else {
						multipartBuilder.addPart(headers, converter.toBody(value, value.getClass()));
					}
				}
			} else if (annotationType == PartMap.class) {
				if (value != null) { // Skip null values.
					String transferEncoding = ((PartMap) annotation).encoding();
					for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
						Object entryKey = entry.getKey();
						if (entryKey == null) {
							throw new IllegalArgumentException(
									"Parameter #" + (i + 1) + " part map contained null key.");
						}
						String entryName = entryKey.toString();
						Object entryValue = entry.getValue();
						Headers headers = Headers.of(
								"Content-Disposition", "name=\"" + entryName + "\"",
								"Content-Transfer-Encoding", transferEncoding);
						if (entryValue != null) { // Skip null values.
							if (entryValue instanceof RequestBody) {
								multipartBuilder.addPart(headers, (RequestBody) entryValue);
							} else if (entryValue instanceof String) {
								multipartBuilder.addPart(headers,
										RequestBody.create(MediaType.parse("text/plain"), (String) entryValue));
							} else {
								multipartBuilder.addPart(headers,
										converter.toBody(entryValue, entryValue.getClass()));
							}
						}
					}
				}
			} */ else if (oAnnotationType == Body.class) {
				if (oValue == null) {
					throw new IllegalArgumentException("Body parameter value must not be null.");
				}
				//Body oBody = (Body) oAnnotation;
				//Class<IWorker<?, InputStream>> oConverterClass = oBody.converter();
				//mConverter = instantiate( oConverterClass );
				
				if (oValue instanceof InputStream) {
					mRequestBody = (InputStream) oValue;
				} else {
					mRequestBody = mConverter.serialize(oValue, oValue.getClass());
				}
			} else {
				throw new IllegalArgumentException(
						"Unknown annotation: " + oAnnotationType.getCanonicalName());
			}
		}
	}

	private void addFormField(String name, String value, boolean encode) {
		/*
		if (encode) {
			formEncodingBuilder.add(name, value);
		} else {
			formEncodingBuilder.addEncoded(name, value);
		}
		*/
		try {
			//Here we're letting the Http Client to encode parameter values.
			// So if encode=true, then we leave the value as is, but if encode=false, we decode the value first.
			mParameters.add( new BasicNameValuePair(name, encode ? value : URLDecoder.decode( value,  "UTF-8") ));
		} catch (UnsupportedEncodingException e) {
			mLogger.error("Error decoding value with UTF-8.", e);
		}
	}
	
	public void addHeader(String name, String value) {
		mLogger.info("addHeader(" + name + ", " + value + ")");
		if (name == null) {
			throw new IllegalArgumentException("Header name must not be null.");
		}
		/*
		if ("Content-Type".equalsIgnoreCase(name)) {
			contentTypeHeader = value;
			return;
		}
		*/
		mHeaders.add(name, value);
	}

	
	/**
	 * WARNING: Not sure here if we should put name/value in mParameters or create a new mQueryParameters Map.
	 * 
	 * @param parameterNumber
	 * @param pMap
	 * @param pEncodeNames
	 * @param pEncodeValues
	 */
	private void addQueryParamMap(int parameterNumber, Map<?, ?> pMap, boolean pEncodeNames, boolean pEncodeValues) {
		for (Map.Entry<?, ?> entry : pMap.entrySet()) {
			Object entryKey = entry.getKey();
			if (entryKey == null) {
				throw new IllegalArgumentException(
						"Parameter #" + (parameterNumber + 1) + " query map contained null key.");
			}
			Object entryValue = entry.getValue();
			if (entryValue != null) { // Skip null values.
				addQueryParam(entryKey.toString(), entryValue.toString(), null, pEncodeNames, pEncodeValues);
			}
		}
	}
	
	@Override
	public void addQueryParam(String pName, Object pValue) {
		addQueryParam(pName, pValue, null, true, true);
	}
	
	public void addQueryParam(String name, Object value, String pValueTemplate, boolean encodeName, boolean encodeValue) {
		if (value instanceof Iterable) {
			for (Object iterableValue : (Iterable<?>) value) {
				if (iterableValue != null) { // Skip null values
					addQueryParam(name, iterableValue.toString(), pValueTemplate, encodeName, encodeValue);
				}
			}
		} else if (value.getClass().isArray()) {
			for (int x = 0, arrayLength = Array.getLength(value); x < arrayLength; x++) {
				Object arrayValue = Array.get(value, x);
				if (arrayValue != null) { // Skip null values
					addQueryParam(name, arrayValue.toString(), pValueTemplate, encodeName, encodeValue);
				}
			}
		} else {
			addQueryParam(name, value.toString(), pValueTemplate, encodeName, encodeValue);
		}
	}

	private void addQueryParam(String pName, String pValue, String pValueTemplate, boolean pEncodeName, boolean pEncodeValue) {
		mLogger.info("addQueryParam(" + pName + ", " + pValue + ", " + pEncodeName + ", " + pEncodeValue + ")");
		if (pName == null) {
			throw new IllegalArgumentException("Query param name must not be null.");
		}
		if (pValue == null) {
			throw new IllegalArgumentException("Query param \"" + pName + "\" value must not be null.");
		}
		String oValue = resolveQueryValue(pValueTemplate, pValue);
		String oName = pName;
		try {
			/*
			StringBuilder queryParams = this.queryParams;
			if (queryParams == null) {
				this.queryParams = queryParams = new StringBuilder();
			}

			queryParams.append(queryParams.length() > 0 ? '&' : '?');
			*/
			/*
			if (pEncodeName) {
				oName = URLEncoder.encode(oName, "UTF-8");
			}
			if (pEncodeValue) {
				oValue = URLEncoder.encode(oValue, "UTF-8");
			}
			*/
			if (!pEncodeName) {
				oName = URLDecoder.decode( oName, "UTF-8");
			}
			if (!pEncodeValue) {
				oValue = URLDecoder.decode( oValue, "UTF-8" );
			}
			
			mParameters.add( new BasicNameValuePair( oName, oValue) );
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(
					"Unable to convert query parameter \"" + pName + "\" value to UTF-8: " + pValue, e);
		}
	}
	
	private String resolveQueryValue(String pValueTemplate, String pValue) {
		if (StringUtils.isEmpty(pValueTemplate)) return pValue;
		return pValueTemplate.replace("{}", pValue);
	}

	public void addPathParam(String pName, String pValue, boolean pUrlEncodeValue) {
		mLogger.info("addPathParam(" + pName + ", " + pValue + ", " + pUrlEncodeValue + ")");
		if (pName == null) {
			throw new IllegalArgumentException("Path replacement name must not be null.");
		}
		if (pValue == null) {
			throw new IllegalArgumentException(
					"Path replacement \"" + pName + "\" value must not be null.");
		}
		try {
			if (pUrlEncodeValue) {
				String encodedValue = URLEncoder.encode(String.valueOf(pValue), "UTF-8");
				// URLEncoder encodes for use as a query parameter. Path encoding uses %20 to
				// encode spaces rather than +. Query encoding difference specified in HTML spec.
				// Any remaining plus signs represent spaces as already URLEncoded.
				encodedValue = encodedValue.replace("+", "%20");
				mResourcePath = mResourcePath.replace("{" + pName + "}", encodedValue);
			} else {
				mResourcePath = mResourcePath.replace("{" + pName + "}", String.valueOf(pValue));
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(
					"Unable to convert path parameter \"" + pName + "\" value to UTF-8:" + pValue, e);
		}
	}
	
	public IHttpRequest build() throws Exception {
		HttpRequest oRequest = new HttpRequest(mEndpointUri);
		oRequest.setExecutionContext(mExecutionContext);
		oRequest.setContent( mRequestBody );
		oRequest.setEndpoint( mEndpointUri );
		for (com.github.lpezet.antiope2.dao.http.Header e : mHeaders.getAllHeaders()) {
			oRequest.addHeader( e.getName(), e.getValue() );
		}
		oRequest.setHttpMethod( mMethodInfo.getRequestMethod() );
		for (NameValuePair e : mParameters) {
			oRequest.addParameter( e.getName(), e.getValue() );
		}
		String oPath = mResourcePath;
		
		if (mMethodInfo.getResourceQuery() != null) {
			//TODO:
			// Here 2 options: add it to the Parameters or append to ResourcePath
			// A 3rd option is to have IHttpRequest hold a Query member (like HttpServletRequest)
			String[] oParams = mMethodInfo.getResourceQuery().split("&");
			for (String oParam : oParams) {
				String[] oNameValue = oParam.split("=");
				oRequest.addParameter(oNameValue[0], URLDecoder.decode( oNameValue[1], "UTF-8"));
			}
		}
		//WARNING: Here I'm not sure. For POST, best put everything in Parameters and put in body of request.
		// For so Retrofit-compatibility sake for now, doing it the Retrofit way.
		/*
		if (!mQueryParameters.isEmpty()) {
			if (oPath.indexOf("?") < 0) oPath += "?";
			StringBuilder oBuilder = new StringBuilder();
			for (Entry<String, V>)
		}
		*/
		oRequest.setResourcePath(oPath);
		//oRequest.setTimeOffset(...);
		
		return oRequest;
	}

//	private HttpMethodName getHttpMethod() {
//		return HttpMethodName.valueOf( mMethodInfo.getRequestMethod() );
//	}

}
