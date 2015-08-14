/**
 * 
 */
package com.github.lpezet.antiope2.retrofitted;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import rx.Observable;

import com.github.lpezet.antiope2.dao.http.IHttpResponse;
import com.github.lpezet.antiope2.retrofitted.Callback;
import com.github.lpezet.antiope2.retrofitted.MethodInfo;
import com.github.lpezet.antiope2.retrofitted.annotation.Converter;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Body;
import com.github.lpezet.antiope2.retrofitted.annotation.http.GET;
import com.github.lpezet.antiope2.retrofitted.annotation.http.POST;
import com.google.gson.reflect.TypeToken;

/**
 * @author Luc Pezet
 */
public class MethodInfoTest {

	@Test
	public void pathParameterParsing() throws Exception {
		expectParams("/");
		expectParams("/foo");
		expectParams("/foo/bar");
		expectParams("/foo/bar/{}");
		expectParams("/foo/bar/{taco}", "taco");
		expectParams("/foo/bar/{t}", "t");
		expectParams("/foo/bar/{!!!}/"); // Invalid parameter.
		expectParams("/foo/bar/{}/{taco}", "taco");
		expectParams("/foo/bar/{taco}/or/{burrito}", "taco", "burrito");
		expectParams("/foo/bar/{taco}/or/{taco}", "taco");
		expectParams("/foo/bar/{taco-shell}", "taco-shell");
		expectParams("/foo/bar/{taco_shell}", "taco_shell");
		expectParams("/foo/bar/{sha256}", "sha256");
		expectParams("/foo/bar/{TACO}", "TACO");
		expectParams("/foo/bar/{taco}/{tAco}/{taCo}", "taco", "tAco", "taCo");
		expectParams("/foo/bar/{1}"); // Invalid parameter, name cannot start with digit.
	}

	private static void expectParams(String path, String... expected) {
		Set<String> calculated = MethodInfo.parsePathParameters(path);
		assertThat(calculated).hasSize(expected.length);
		if (expected.length > 0) {
			assertThat(calculated).containsExactly(expected);
		}
	}

	static class Dummy {
	}
	
	static class MyConverter implements com.github.lpezet.antiope2.retrofitted.converter.Converter {
		@Override
		public Object deserialize(InputStream pBody, Type pType) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public InputStream serialize(Object pBody, Type pType) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	@Test
	public void methodSpecificConverter() {
		
		class Example {
			@Converter(MyConverter.class)
			@GET("/foo")
			String a() {
				return null;
			}
		}
		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);
		assertThat(methodInfo.getConverter().getClass()).isEqualTo(MyConverter.class);
	}

	@Test
	public void concreteBodyType() {
		class Example {
			@POST("/foo")
			String a(@Body Dummy body) {
				return null;
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);
		assertThat(methodInfo.getRequestObjectType()).isEqualTo(Dummy.class);
	}

	@Test
	public void genericBodyType() {
		class Example {
			@POST("/foo")
			String a(@Body List<String> body) {
				return null;
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);
		Type expected = new TypeToken<List<String>>() {}.getType();
		assertThat(methodInfo.getRequestObjectType()).isEqualTo(expected);
	}

	@Test
	public void wildcardBodyType() {
		class Example {
			@POST("/foo")
			String a(@Body List<? super String> body) {
				return null;
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);
		Type expected = new TypeToken<List<? super String>>() {}.getType();
		assertThat(methodInfo.getRequestObjectType()).isEqualTo(expected);
	}

	@Test
	public void concreteCallbackTypes() {
		class Example {
			@GET("/foo")
			void a(ResponseCallback cb) {
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);
		assertThat(methodInfo.getResponseObjectType()).isEqualTo(IHttpResponse.class);
	}

	@Test
	public void genericCallbackTypes() {
		class Example {
			@GET("/foo")
			void a(Callback<IHttpResponse> cb) {
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);
		assertThat(methodInfo.getResponseObjectType()).isEqualTo(IHttpResponse.class);
	}

	@Test
	public void wildcardGenericCallbackTypes() {
		class Example {
			@GET("/foo")
			void a(Callback<? extends IHttpResponse> c) {
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);
		assertThat(methodInfo.getResponseObjectType()).isEqualTo(IHttpResponse.class);
	}

	@Test
	public void genericCallbackWithGenericType() {
		class Example {
			@GET("/foo")
			void a(Callback<List<String>> c) {
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);

		Type expected = new TypeToken<List<String>>() {}.getType();
		assertThat(methodInfo.getResponseObjectType()).isEqualTo(expected);
	}

	// RestMethodInfo reconstructs this type from MultimapCallback<String, Set<Long>>. It contains
	// a little of everything: a parameterized type, a generic array, and a wildcard.
	private static Map<? extends String, Set<Long>[]>	extendingGenericCallbackType;

	@Test
	public void extendingGenericCallback() throws Exception {
		class Example {
			@GET("/foo")
			void a(MultimapCallback<String, Set<Long>> callback) {
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);
		assertThat(methodInfo.getResponseObjectType()).isEqualTo(
				MethodInfoTest.class.getDeclaredField("extendingGenericCallbackType").getGenericType());
	}

	@Test
	public void synchronousResponse() {
		class Example {
			@GET("/foo")
			IHttpResponse a() {
				return null;
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);
		assertThat(methodInfo.getResponseObjectType()).isEqualTo(IHttpResponse.class);
	}

	@Test
	public void synchronousGenericResponse() {
		class Example {
			@GET("/foo")
			List<String> a() {
				return null;
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);

		Type expected = new TypeToken<List<String>>() {}.getType();
		assertThat(methodInfo.getResponseObjectType()).isEqualTo(expected);
	}

	// TODO
	/*
	 * @Test public void streamingResponse() {
	 * class Example {
	 * @GET("/foo") @Streaming Response a() {
	 * return null;
	 * }
	 * }
	 * Method method = TestingUtils.onlyMethod(Example.class);
	 * MethodInfo methodInfo = new MethodInfo(method);
	 * assertThat(methodInfo.responseObjectType).isEqualTo(Response.class);
	 * }
	 */
	@Test
	public void observableResponse() {
		class Example {
			@GET("/foo")
			Observable<IHttpResponse> a() {
				return null;
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);
		assertThat(methodInfo.getResponseObjectType()).isEqualTo(IHttpResponse.class);
	}

	@Test
	public void observableGenericResponse() {
		class Example {
			@GET("/foo")
			Observable<List<String>> a() {
				return null;
			}
		}

		Method method = TestingUtils.onlyMethod(Example.class);
		MethodInfo methodInfo = new MethodInfo(method);
		Type expected = new TypeToken<List<String>>() {}.getType();
		assertThat(methodInfo.getResponseObjectType()).isEqualTo(expected);
	}

	private static interface ResponseCallback extends Callback<IHttpResponse> {
	}

	private static interface MultimapCallback<K, V> extends Callback<Map<? extends K, V[]>> {
	}
}
