/**
 * 
 */
package com.github.lpezet.antiope2.retrofitted;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;

import com.github.lpezet.antiope2.retrofitted.annotation.Converter;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Body;
import com.github.lpezet.antiope2.retrofitted.annotation.http.DELETE;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Field;
import com.github.lpezet.antiope2.retrofitted.annotation.http.FieldMap;
import com.github.lpezet.antiope2.retrofitted.annotation.http.FormUrlEncoded;
import com.github.lpezet.antiope2.retrofitted.annotation.http.GET;
import com.github.lpezet.antiope2.retrofitted.annotation.http.HEAD;
import com.github.lpezet.antiope2.retrofitted.annotation.http.HTTP;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Header;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Headers;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Multipart;
import com.github.lpezet.antiope2.retrofitted.annotation.http.PATCH;
import com.github.lpezet.antiope2.retrofitted.annotation.http.POST;
import com.github.lpezet.antiope2.retrofitted.annotation.http.PUT;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Part;
import com.github.lpezet.antiope2.retrofitted.annotation.http.PartMap;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Path;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Query;
import com.github.lpezet.antiope2.retrofitted.annotation.http.QueryMap;

/**
 * @author Luc Pezet
 */
public class MethodInfo {

	private static final String		PARAM				= "[a-zA-Z][a-zA-Z0-9_-]*";
	private static final Pattern	PARAM_NAME_REGEX	= Pattern.compile(PARAM);
	private static final Pattern	PARAM_URL_REGEX		= Pattern.compile("\\{(" + PARAM + ")\\}");

	enum ExecutionType {
		ASYNC,
		RX,
		SYNC
	}

	enum RequestType {
		/** No content-specific logic required. */
		SIMPLE,
		/** Multi-part request body. */
		MULTIPART,
		/** Form URL-encoded request body. */
		FORM_URL_ENCODED
	}

	private Method												mMethod;
	private final ExecutionType									mExecutionType;
	private Type												mResponseObjectType;
	private RequestType											mRequestType	= RequestType.SIMPLE;

	// Not yet supported but added for now (will always return false)
	private final boolean										mStreaming		= false;

	private Type												mRequestObjectType;
	private boolean												mRequestHasBody;
	private String												mRequestMethod;
	private com.github.lpezet.antiope2.dao.http.Headers			mHeaders		= new com.github.lpezet.antiope2.dao.http.Headers();
	private String												mResourcePath;
	private String												mResourceQuery;
	private Set<String>											mResourcePathParams;
	private com.github.lpezet.antiope2.retrofitted.converter.Converter	mConverter;
	private Annotation[]										mParamAnnotations;

	public MethodInfo(Method pMethod) {
		mMethod = pMethod;

		mExecutionType = parseResponseType();
		parseMethodAnnotations();
		parseParameters();
	}

	public Type getResponseObjectType() {
		return mResponseObjectType;
	}

	public Type getRequestObjectType() {
		return mRequestObjectType;
	}

	public ExecutionType getExecutionType() {
		return mExecutionType;
	}

	public boolean isAsync() {
		return mExecutionType == ExecutionType.ASYNC;
	}

	public Annotation[] getParamAnnotations() {
		return mParamAnnotations;
	}

	public String getResourcePath() {
		return mResourcePath;
	}

	public String getRequestMethod() {
		return mRequestMethod;
	}

	public String getResourceQuery() {
		return mResourceQuery;
	}

	public List<com.github.lpezet.antiope2.dao.http.Header> getHeaders() {
		return mHeaders.getAllHeaders();
	}

	public boolean isStreaming() {
		return mStreaming;
	}

	public com.github.lpezet.antiope2.retrofitted.converter.Converter getConverter() {
		return mConverter;
	}

	// ##############################################################
	// Parse Response Type
	// ##############################################################

	private ExecutionType parseResponseType() {
		// Synchronous methods have a non-void return type.
		// Observable methods have a return type of Observable.
		Type returnType = mMethod.getGenericReturnType();

		// Asynchronous methods should have a Callback type as the last argument.
		Type lastArgType = null;
		Class<?> lastArgClass = null;
		Type[] parameterTypes = mMethod.getGenericParameterTypes();
		if (parameterTypes.length > 0) {
			Type typeToCheck = parameterTypes[parameterTypes.length - 1];
			lastArgType = typeToCheck;
			if (typeToCheck instanceof ParameterizedType) {
				typeToCheck = ((ParameterizedType) typeToCheck).getRawType();
			}
			if (typeToCheck instanceof Class) {
				lastArgClass = (Class<?>) typeToCheck;
			}
		}

		boolean hasReturnType = returnType != void.class;
		boolean hasCallback = lastArgClass != null && Callback.class.isAssignableFrom(lastArgClass);

		// Check for invalid configurations.
		if (hasReturnType && hasCallback) {
			throw methodError("Must have return type or Callback as last argument, not both.");
		}
		if (!hasReturnType && !hasCallback) {
			throw methodError("Must have either a return type or Callback as last argument.");
		}

		if (hasReturnType) {
			if (Platform.HAS_RX_JAVA) {
				Class rawReturnType = Types.getRawType(returnType);
				if (RxSupport.isObservable(rawReturnType)) {
					returnType = RxSupport.getObservableType(returnType, rawReturnType);
					mResponseObjectType = getParameterUpperBound((ParameterizedType) returnType);
					return ExecutionType.RX;
				}
			}
			mResponseObjectType = returnType;
			return ExecutionType.SYNC;
		}

		lastArgType = Types.getSupertype(lastArgType, Types.getRawType(lastArgType), Callback.class);
		if (lastArgType instanceof ParameterizedType) {
			mResponseObjectType = getParameterUpperBound((ParameterizedType) lastArgType);
			return ExecutionType.ASYNC;
		}

		throw methodError("Last parameter must be of type Callback<X> or Callback<? super X>.");
	}

	private RuntimeException methodError(String message, Object... args) {
		if (args.length > 0) {
			message = String.format(message, args);
		}
		return new IllegalArgumentException(
				mMethod.getDeclaringClass().getSimpleName() + "." + mMethod.getName() + ": " + message);
	}

	/** Indirection to avoid log complaints if RxJava isn't present. */
	private static final class RxSupport {
		public static boolean isObservable(Class rawType) {
			return rawType == Observable.class;
		}

		public static Type getObservableType(Type contextType, Class contextRawType) {
			return Types.getSupertype(contextType, contextRawType, Observable.class);
		}
	}

	private static Type getParameterUpperBound(ParameterizedType type) {
		Type[] types = type.getActualTypeArguments();
		for (int i = 0; i < types.length; i++) {
			Type paramType = types[i];
			if (paramType instanceof WildcardType) {
				types[i] = ((WildcardType) paramType).getUpperBounds()[0];
			}
		}
		return types[0];
	}

	// ######################################################
	// Parse Method Annotations
	// ######################################################
	/** Loads {@link #mRequestMethod} and {@link #requestType}. */
	private void parseMethodAnnotations() {
		for (Annotation methodAnnotation : mMethod.getAnnotations()) {
			Class<? extends Annotation> annotationType = methodAnnotation.annotationType();
			if (annotationType == com.github.lpezet.antiope2.retrofitted.annotation.Converter.class) {
				mConverter = createConverter((com.github.lpezet.antiope2.retrofitted.annotation.Converter) methodAnnotation);
			} else if (annotationType == DELETE.class) {
				parseHttpMethodAndPath("DELETE", ((DELETE) methodAnnotation).value(), false);
			} else if (annotationType == GET.class) {
				parseHttpMethodAndPath("GET", ((GET) methodAnnotation).value(), false);
			} else if (annotationType == HEAD.class) {
				parseHttpMethodAndPath("HEAD", ((HEAD) methodAnnotation).value(), false);
			} else if (annotationType == PATCH.class) {
				parseHttpMethodAndPath("PATCH", ((PATCH) methodAnnotation).value(), true);
			} else if (annotationType == POST.class) {
				parseHttpMethodAndPath("POST", ((POST) methodAnnotation).value(), true);
			} else if (annotationType == PUT.class) {
				parseHttpMethodAndPath("PUT", ((PUT) methodAnnotation).value(), true);
			} else if (annotationType == HTTP.class) {
				HTTP http = (HTTP) methodAnnotation;
				parseHttpMethodAndPath(http.method(), http.path(), http.hasBody());
			} else if (annotationType == Headers.class) {
				String[] headersToParse = ((Headers) methodAnnotation).value();
				if (headersToParse.length == 0) {
					throw methodError("@Headers annotation is empty.");
				}
				mHeaders = parseHeaders(headersToParse);
			} else if (annotationType == Multipart.class) {
				if (mRequestType != RequestType.SIMPLE) {
					throw methodError("Only one encoding annotation is allowed.");
				}
				mRequestType = RequestType.MULTIPART;
			} else if (annotationType == FormUrlEncoded.class) {
				if (mRequestType != RequestType.SIMPLE) {
					throw methodError("Only one encoding annotation is allowed.");
				}
				mRequestType = RequestType.FORM_URL_ENCODED;
			}
			/*
			 * TODO: Implement
			 * else if (annotationType == Streaming.class) {
			 * if (responseObjectType != Response.class) {
			 * throw methodError(
			 * "Only methods having %s as data type are allowed to have @%s annotation.",
			 * Response.class.getSimpleName(), Streaming.class.getSimpleName());
			 * }
			 * isStreaming = true;
			 * }
			 */
		}

		if (mRequestMethod == null) {
			throw methodError("HTTP method annotation is required (e.g., @GET, @POST, etc.).");
		}
		if (!mRequestHasBody) {
			if (mRequestType == RequestType.MULTIPART) {
				throw methodError("Multipart can only be specified on HTTP methods with request body (e.g., @POST).");
			}
			if (mRequestType == RequestType.FORM_URL_ENCODED) {
				throw methodError("FormUrlEncoded can only be specified on HTTP methods with request body "
						+ "(e.g., @POST).");
			}
		}
	}

	private com.github.lpezet.antiope2.retrofitted.converter.Converter createConverter(Converter pMethodAnnotation) {
		try {
			return pMethodAnnotation.value().newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/** Loads {@link #requestUrl}, {@link #requestUrlParamNames}, and {@link #requestQuery}. */
	private void parseHttpMethodAndPath(String pMethod, String pPath, boolean pHasBody) {
		if (mRequestMethod != null) {
			throw methodError("Only one HTTP method is allowed. Found: %s and %s.", mRequestMethod,
					pMethod);
		}
		if (pPath == null || pPath.length() == 0 || pPath.charAt(0) != '/') {
			throw methodError("URL path \"%s\" must start with '/'.", pPath);
		}

		// Get the relative URL path and existing query string, if present.
		String url = pPath;
		String query = null;
		int question = pPath.indexOf('?');
		if (question != -1 && question < pPath.length() - 1) {
			url = pPath.substring(0, question);
			query = pPath.substring(question + 1);

			// Ensure the query string does not have any named parameters.
			Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(query);
			if (queryParamMatcher.find()) {
				throw methodError("URL query string \"%s\" must not have replace block. For dynamic query"
						+ " parameters use @Query.", query);
			}
		}

		mRequestMethod = pMethod;
		mResourcePath = url;
		mRequestHasBody = pHasBody;
		mResourceQuery = query;
		mResourcePathParams = parsePathParameters(pPath);
	}

	/**
	 * Gets the set of unique path parameters used in the given URI. If a parameter is used twice
	 * in the URI, it will only show up once in the set.
	 */
	static Set<String> parsePathParameters(String path) {
		Matcher m = PARAM_URL_REGEX.matcher(path);
		Set<String> patterns = new LinkedHashSet<String>();
		while (m.find()) {
			patterns.add(m.group(1));
		}
		return patterns;
	}

	private com.github.lpezet.antiope2.dao.http.Headers parseHeaders(String[] pHeaders) {
		com.github.lpezet.antiope2.dao.http.Headers oHeaders = new com.github.lpezet.antiope2.dao.http.Headers();
		for (String header : pHeaders) {
			int colon = header.indexOf(':');
			if (colon == -1 || colon == 0 || colon == header.length() - 1) {
				throw methodError("@Headers value must be in the form \"Name: Value\". Found: \"%s\"",
						header);
			}
			String oName = header.substring(0, colon);
			String oValue = header.substring(colon + 1).trim();
			oHeaders.add(oName, oValue);
		}
		return oHeaders;
	}

	// #########################################
	// Parse Parameters
	// #########################################
	/**
	 * Loads {@link #requestParamAnnotations}. Must be called after
	 * {@link #parseMethodAnnotations()}.
	 */
	private void parseParameters() {
		Type[] methodParameterTypes = mMethod.getGenericParameterTypes();

		Annotation[][] methodParameterAnnotationArrays = mMethod.getParameterAnnotations();
		int count = methodParameterAnnotationArrays.length;
		if (mExecutionType == ExecutionType.ASYNC) {
			count -= 1; // Callback is last argument when not a synchronous method.
		}

		Annotation[] requestParamAnnotations = new Annotation[count];

		boolean gotField = false;
		boolean gotPart = false;
		boolean gotBody = false;

		for (int i = 0; i < count; i++) {
			Type methodParameterType = methodParameterTypes[i];
			Annotation[] methodParameterAnnotations = methodParameterAnnotationArrays[i];
			if (methodParameterAnnotations != null) {
				for (Annotation methodParameterAnnotation : methodParameterAnnotations) {
					Class<? extends Annotation> methodAnnotationType =
							methodParameterAnnotation.annotationType();

					if (methodAnnotationType == Path.class) {
						String name = ((Path) methodParameterAnnotation).value();
						validatePathName(i, name);
					} else if (methodAnnotationType == Query.class) {
						// Nothing to do.
					} else if (methodAnnotationType == QueryMap.class) {
						if (!Map.class.isAssignableFrom(Types.getRawType(methodParameterType))) {
							throw parameterError(i, "@QueryMap parameter type must be Map.");
						}
					} else if (methodAnnotationType == Header.class) {
						// Nothing to do.
					} else if (methodAnnotationType == Field.class) {
						if (mRequestType != RequestType.FORM_URL_ENCODED) {
							throw parameterError(i, "@Field parameters can only be used with form encoding.");
						}

						gotField = true;
					} else if (methodAnnotationType == FieldMap.class) {
						if (mRequestType != RequestType.FORM_URL_ENCODED) {
							throw parameterError(i, "@FieldMap parameters can only be used with form encoding.");
						}
						if (!Map.class.isAssignableFrom(Types.getRawType(methodParameterType))) {
							throw parameterError(i, "@FieldMap parameter type must be Map.");
						}

						gotField = true;
					} else if (methodAnnotationType == Part.class) {
						if (mRequestType != RequestType.MULTIPART) {
							throw parameterError(i, "@Part parameters can only be used with multipart encoding.");
						}

						gotPart = true;
					} else if (methodAnnotationType == PartMap.class) {
						if (mRequestType != RequestType.MULTIPART) {
							throw parameterError(i,
									"@PartMap parameters can only be used with multipart encoding.");
						}
						if (!Map.class.isAssignableFrom(Types.getRawType(methodParameterType))) {
							throw parameterError(i, "@PartMap parameter type must be Map.");
						}

						gotPart = true;
					} else if (methodAnnotationType == Body.class) {
						if (mRequestType != RequestType.SIMPLE) {
							throw parameterError(i,
									"@Body parameters cannot be used with form or multi-part encoding.");
						}
						if (gotBody) {
							throw methodError("Multiple @Body method annotations found.");
						}

						mRequestObjectType = methodParameterType;
						gotBody = true;
					} else {
						// This is a non-Retrofit annotation. Skip to the next one.
						continue;
					}

					if (requestParamAnnotations[i] != null) {
						throw parameterError(i,
								"Multiple Retrofit annotations found, only one allowed: @%s, @%s.",
								requestParamAnnotations[i].annotationType().getSimpleName(),
								methodAnnotationType.getSimpleName());
					}
					requestParamAnnotations[i] = methodParameterAnnotation;
				}
			}

			if (requestParamAnnotations[i] == null) {
				throw parameterError(i, "No Retrofit annotation found.");
			}
		}

		if (mRequestType == RequestType.SIMPLE && !mRequestHasBody && gotBody) {
			throw methodError("Non-body HTTP method cannot contain @Body or @TypedOutput.");
		}
		if (mRequestType == RequestType.FORM_URL_ENCODED && !gotField) {
			throw methodError("Form-encoded method must contain at least one @Field.");
		}
		if (mRequestType == RequestType.MULTIPART && !gotPart) {
			throw methodError("Multipart method must contain at least one @Part.");
		}

		mParamAnnotations = requestParamAnnotations;
	}

	private void validatePathName(int index, String name) {
		if (!PARAM_NAME_REGEX.matcher(name).matches()) {
			throw parameterError(index, "@Path parameter name must match %s. Found: %s",
					PARAM_URL_REGEX.pattern(), name);
		}
		// Verify URL replacement name is actually present in the URL path.
		if (!mResourcePathParams.contains(name)) {
			throw parameterError(index, "URL \"%s\" does not contain \"{%s}\".", mResourcePath, name);
		}
	}

	private RuntimeException parameterError(int index, String message, Object... args) {
		return methodError(message + " (parameter #" + (index + 1) + ")", args);
	}
}
