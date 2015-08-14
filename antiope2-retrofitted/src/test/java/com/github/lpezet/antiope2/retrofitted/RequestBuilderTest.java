/**
 * 
 */
package com.github.lpezet.antiope2.retrofitted;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import rx.Observable;

import com.github.lpezet.antiope2.dao.http.BasicNameValuePair;
import com.github.lpezet.antiope2.dao.http.IHttpRequest;
import com.github.lpezet.antiope2.dao.http.NameValuePair;
import com.github.lpezet.antiope2.retrofitted.Callback;
import com.github.lpezet.antiope2.retrofitted.MethodInfo;
import com.github.lpezet.antiope2.retrofitted.RequestBuilder;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Body;
import com.github.lpezet.antiope2.retrofitted.annotation.http.DELETE;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Field;
import com.github.lpezet.antiope2.retrofitted.annotation.http.FieldMap;
import com.github.lpezet.antiope2.retrofitted.annotation.http.FormUrlEncoded;
import com.github.lpezet.antiope2.retrofitted.annotation.http.GET;
import com.github.lpezet.antiope2.retrofitted.annotation.http.HEAD;
import com.github.lpezet.antiope2.retrofitted.annotation.http.HTTP;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Header;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Multipart;
import com.github.lpezet.antiope2.retrofitted.annotation.http.PATCH;
import com.github.lpezet.antiope2.retrofitted.annotation.http.POST;
import com.github.lpezet.antiope2.retrofitted.annotation.http.PUT;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Part;
import com.github.lpezet.antiope2.retrofitted.annotation.http.PartMap;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Path;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Query;
import com.github.lpezet.antiope2.retrofitted.annotation.http.QueryMap;
import com.github.lpezet.antiope2.retrofitted.annotation.http.Streaming;
import com.github.lpezet.antiope2.retrofitted.converter.Converter;
import com.github.lpezet.antiope2.retrofitted.converter.GsonConverter;
import com.google.gson.Gson;

/**
 * @author Luc Pezet
 *
 */
public class RequestBuilderTest {

	@Test public void custom1Method() {
	    class Example {
	      @HTTP(method = "CUSTOM1", path = "/foo")
	      String method() {
	        return null;
	      }
	    }

	    IHttpRequest request = buildRequest(Example.class);
	    assertThat(request.getHttpMethod()).isEqualTo("CUSTOM1");
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo");
	    assertThat(request.getContent()).isNull();
	  }

	  @Ignore // TODO https://github.com/square/okhttp/issues/229
	  @Test public void custom2Method() {
	    class Example {
	      @HTTP(method = "CUSTOM2", path = "/foo", hasBody = true)
	      String method(@Body InputStream body) {
	        return null;
	      }
	    }

	    //RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "hi");
	    InputStream body = new ByteArrayInputStream( "{ \"a\": \"b\" }".getBytes() );
	    IHttpRequest request = buildRequest(Example.class, body);
	    assertThat(request.getHttpMethod()).isEqualTo("CUSTOM2");
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/foo");
	    assertBody(request.getContent(), "hi");
	  }

	  @Test public void onlyOneEncodingIsAllowedMultipartFirst() {
	    class Example {
	      @Multipart //
	      @FormUrlEncoded //
	      @POST("/") //
	      String method() {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Example.method: Only one encoding annotation is allowed.");
	    }
	  }

	  @Test public void onlyOneEncodingIsAllowedFormEncodingFirst() {
	    class Example {
	      @FormUrlEncoded //
	      @Multipart //
	      @POST("/") //
	      String method() {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Example.method: Only one encoding annotation is allowed.");
	    }
	  }

	  @Test public void invalidPathParam() throws Exception {
	    class Example {
	      @GET("/") //
	      String method(@Path("hey!") String thing) {
	        return null;
	      }
	    }

	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: @Path parameter name must match \\{([a-zA-Z][a-zA-Z0-9_-]*)\\}."
	              + " Found: hey! (parameter #1)");
	    }
	  }

	  @Test public void pathParamNotAllowedInQuery() throws Exception {
	    class Example {
	      @GET("/foo?bar={bar}") //
	      String method(@Path("bar") String thing) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: URL query string \"bar={bar}\" must not have replace block."
	              + " For dynamic query parameters use @Query.");
	    }
	  }

	  @Test public void multipleParameterAnnotationsNotAllowed() throws Exception {
	    class Example {
	      @GET("/") //
	      String method(@Body @Query("nope") Object o) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: Multiple Retrofit annotations found, only one allowed:"
	              + " @Body, @Query. (parameter #1)");
	    }
	  }

	  @Test public void twoMethodsFail() {
	    class Example {
	      @PATCH("/foo") //
	      @POST("/foo") //
	      String method() {
	        return null;
	      }
	    }

	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: Only one HTTP method is allowed. Found: PATCH and POST.");
	    }
	  }

	  @Test public void pathMustBePrefixedWithSlash() {
	    class Example {
	      @GET("foo/bar") //
	      String method() {
	        return null;
	      }
	    }

	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Example.method: URL path \"foo/bar\" must start with '/'.");
	    }
	  }

	  //TODO: Support Streaming
	  @Ignore
	  @Test public void streamingResponseNotAllowed() {
	    class Example {
	      @GET("/foo") //
	      @Streaming //
	      String method() {
	        return null;
	      }
	    }

	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: Only methods having Response as data type are allowed to have @Streaming annotation.");
	    }
	  }

	  //TODO: Support Streaming
	  @Ignore
	  @Test public void streamingResponseWithCallbackNotAllowed() {
	    class Example {
	      @GET("/foo") //
	      @Streaming //
	      void method(Callback<String> callback) {
	      }
	    }

	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: Only methods having Response as data type are allowed to have @Streaming annotation.");
	    }
	  }

	  @Test public void observableWithCallback() {
	    class Example {
	      @GET("/foo") //
	      Observable<String> method(Callback<String> callback) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: Must have return type or Callback as last argument, not both.");
	    }
	  }

	  @Test public void missingCallbackTypes() {
	    class Example {
	      @GET("/foo") //
	      void method(@Query("id") String id) {
	      }
	    }

	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: Must have either a return type or Callback as last argument.");
	    }
	  }

	  @Test public void nonParameterizedCallbackFails() {
	    class Example {
	      @GET("/foo") //
	      void method(Callback cb) {
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: Last parameter must be of type Callback<X> or Callback<? super X>.");
	    }
	  }

	  @Test public void synchronousWithAsyncCallback() {
	    class Example {
	      @GET("/foo") //
	      String method(Callback<String> callback) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: Must have return type or Callback as last argument, not both.");
	    }
	  }

	  @Test public void lackingMethod() {
	    class Example {
	      String method() {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: HTTP method annotation is required (e.g., @GET, @POST, etc.).");
	    }
	  }

	  @Test public void implicitMultipartForbidden() {
	    class Example {
	      @POST("/") //
	      String method(@Part("a") int a) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: @Part parameters can only be used with multipart encoding. (parameter #1)");
	    }
	  }

	  @Test public void implicitMultipartWithPartMapForbidden() {
	    class Example {
	      @POST("/") //
	      String method(@PartMap Map<String, String> params) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: @PartMap parameters can only be used with multipart encoding. (parameter #1)");
	    }
	  }

	  @Test public void multipartFailsOnNonBodyMethod() {
	    class Example {
	      @Multipart //
	      @GET("/") //
	      String method() {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: Multipart can only be specified on HTTP methods with request body (e.g., @POST).");
	    }
	  }

	  @Test public void multipartFailsWithNoParts() {
	    class Example {
	      @Multipart //
	      @POST("/") //
	      String method() {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Example.method: Multipart method must contain at least one @Part.");
	    }
	  }

	  @Test public void implicitFormEncodingByFieldForbidden() {
	    class Example {
	      @POST("/") //
	      String method(@Field("a") int a) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: @Field parameters can only be used with form encoding. (parameter #1)");
	    }
	  }

	  @Test public void implicitFormEncodingByFieldMapForbidden() {
	    class Example {
	      @POST("/") //
	      String method(@FieldMap Map<String, String> a) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: @FieldMap parameters can only be used with form encoding. (parameter #1)");
	    }
	  }

	  //@Test public void formEncodingFailsOnNonBodyMethod() {
	  //  class Example {
	  //    @FormUrlEncoded //
	  //    @GET("/") //
	  //    String method() {
	  //      return null;
	  //    }
	  //  }
	  //  try {
	  //    buildRequest(Example.class);
	  //    fail();
	  //  } catch (IllegalArgumentException e) {
	  //    assertThat(e).hasMessage(
	  //        "Example.method: FormUrlEncoded can only be specified on HTTP methods with request body (e.g., @POST).");
	  //  }
	  //}
	  //
	  //@Test public void formEncodingFailsWithNoParts() {
	  //  class Example {
	  //    @FormUrlEncoded //
	  //    @POST("/") //
	  //    String method() {
	  //      return null;
	  //    }
	  //  }
	  //  try {
	  //    buildRequest(Example.class);
	  //    fail();
	  //  } catch (IllegalArgumentException e) {
	  //    assertThat(e).hasMessage("Example.method: Form-encoded method must contain at least one @Field.");
	  //  }
	  //}

	  @Test public void headersFailWhenEmptyOnMethod() {
	    class Example {
	      @GET("/") //
	      @com.github.lpezet.antiope2.retrofitted.annotation.http.Headers({}) //
	      String method() {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Example.method: @Headers annotation is empty.");
	    }
	  }

	  @Test public void headersFailWhenMalformed() {
	    class Example {
	      @GET("/") //
	      @com.github.lpezet.antiope2.retrofitted.annotation.http.Headers("Malformed") //
	      String method() {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: @Headers value must be in the form \"Name: Value\". Found: \"Malformed\"");
	    }
	  }

	  @Test public void pathParamNonPathParamAndTypedBytes() {
	    class Example {
	      @PUT("/{a}") //
	      String method(@Path("a") int a, @Path("b") int b, @Body int c) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Example.method: URL \"/{a}\" does not contain \"{b}\". (parameter #2)");
	    }
	  }

	  @Test public void parameterWithoutAnnotation() {
	    class Example {
	      @GET("/") //
	      String method(String a) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Example.method: No Retrofit annotation found. (parameter #1)");
	    }
	  }

	  @Test public void nonBodyHttpMethodWithSingleEntity() {
	    class Example {
	      @GET("/") //
	      String method(@Body Object o) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: Non-body HTTP method cannot contain @Body or @TypedOutput.");
	    }
	  }

	  @Test public void queryMapMustBeAMap() {
	    class Example {
	      @GET("/") //
	      String method(@QueryMap List<String> a) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Example.method: @QueryMap parameter type must be Map. (parameter #1)");
	    }
	  }

	  @Test public void queryMapRejectsNullKeys() {
	    class Example {
	      @GET("/") //
	      String method(@QueryMap Map<String, String> a) {
	        return null;
	      }
	    }

	    Map<String, String> queryParams = new LinkedHashMap<String, String>();
	    queryParams.put("ping", "pong");
	    queryParams.put(null, "kat");

	    try {
	      buildRequest(Example.class, queryParams);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Parameter #1 query map contained null key.");
	    }
	  }

	  @Test public void twoBodies() {
	    class Example {
	      @PUT("/") //
	      String method(@Body int o1, @Body int o2) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Example.method: Multiple @Body method annotations found.");
	    }
	  }

	  @Test public void bodyInNonBodyRequest() {
	    class Example {
	      @Multipart //
	      @PUT("/") //
	      String method(@Part("one") int o1, @Body int o2) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage(
	          "Example.method: @Body parameters cannot be used with form or multi-part encoding. (parameter #2)");
	    }
	  }

	  @Test public void get() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method() {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class);
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void delete() {
	    class Example {
	      @DELETE("/foo/bar/") //
	      String method() {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class);
	    assertThat(request.getHttpMethod()).isEqualTo("DELETE");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertNull(request.getContent());
	  }

	  @Test public void head() {
	    class Example {
	      @HEAD("/foo/bar/") //
	      String method() {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class);
	    assertThat(request.getHttpMethod()).isEqualTo("HEAD");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void post() {
	    class Example {
	      @POST("/foo/bar/") //
	      String method(@Body InputStream body) {
	        return null;
	      }
	    }
	    //RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "hi");
	    InputStream body = new ByteArrayInputStream( "hi".getBytes() );
	    IHttpRequest request = buildRequest(Example.class, body);
	    assertThat(request.getHttpMethod()).isEqualTo("POST");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	     assertBody(request.getContent(), "hi");
	  }

	  @Test public void put() {
	    class Example {
	      @PUT("/foo/bar/") //
	      String method(@Body InputStream body) {
	        return null;
	      }
	    }
	    //RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "hi");
	    InputStream body = new ByteArrayInputStream( "hi".getBytes() );
	    IHttpRequest request = buildRequest(Example.class, body);
	    assertThat(request.getHttpMethod()).isEqualTo("PUT");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertBody(request.getContent(), "hi");
	  }

	  @Test public void patch() {
	    class Example {
	      @PATCH("/foo/bar/") //
	      String method(@Body InputStream body) {
	        return null;
	      }
	    }
	    //RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "hi");
	    InputStream body = new ByteArrayInputStream( "hi".getBytes() );
	    IHttpRequest request = buildRequest(Example.class, body);
	    assertThat(request.getHttpMethod()).isEqualTo("PATCH");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertBody(request.getContent(), "hi");
	  }

	  @Test public void getWithPathParam() {
	    class Example {
	      @GET("/foo/bar/{ping}/") //
	      String method(@Path("ping") String ping) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "po ng");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/po%20ng/");
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithEncodedPathParam() {
	    class Example {
	      @GET("/foo/bar/{ping}/") //
	      String method(@Path(value = "ping", encode = false) String ping) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "po%20ng");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/po%20ng/");
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void pathParamRequired() {
	    class Example {
	      @GET("/foo/bar/{ping}/") //
	      String method(@Path("ping") String ping) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class, new Object[] { null });
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e.getMessage()).isEqualTo("Path parameter \"ping\" value must not be null.");
	    }
	  }
	  
	  @Test
	  public void queryWithValueTemplate() {
		  class Example {
		      @GET("/foo/bar/") //
		      String method(@Query(value="q", template="this is a {} template") String q) {
		        return null;
		      }
		    }
		    IHttpRequest request = buildRequest(Example.class, "great");
		    assertThat(request.getHttpMethod()).isEqualTo("GET");
		    assertThat(request.getHeaders().size()).isZero();
		    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
		    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
		    assertThat(request.getParameters()).containsOnly(nameValuePair("q", "this is a great template"));
		    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithQueryParam() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Query("ping") String ping) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "pong");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("ping", "pong"));
	    assertThat(request.getContent()).isNull();
	  }

	  private static NameValuePair nameValuePair(String pName, String pValue) {
		  return new BasicNameValuePair(pName, pValue);
	  }
	  

	@Test public void getWithEncodedQueryParam() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Query(value = "ping", encodeValue = false) String ping) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "p%20o%20n%20g");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("ping", "p o n g"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithEncodeNameQueryParam() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Query(value = "pi ng", encodeName = true) String ping) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "pong");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("pi ng", "pong"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithEncodeNameEncodedValueQueryParam() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Query(value = "pi ng", encodeName = true, encodeValue = false) String ping) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "po%20ng");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsExactly(nameValuePair("pi ng", "po ng"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void queryParamOptionalOmitsQuery() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Query("ping") String ping) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, new Object[] { null });
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).isEmpty();
	 }

	  @Test public void queryParamOptional() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Query("foo") String foo, @Query("ping") String ping,
	          @Query("kit") String kit) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "bar", null, "kat");
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("foo", "bar"), nameValuePair("kit", "kat"));
	  }

	  @Test public void getWithQueryUrlAndParam() {
	    class Example {
	      @GET("/foo/bar/?hi=mom") //
	      String method(@Query("ping") String ping) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "pong");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("hi", "mom"), nameValuePair("ping", "pong"));
	    //assertThat(request.getEndpoint().toString() + request.getResourcePath()).isEqualTo("http://example.com/foo/bar/?hi=mom&ping=pong");
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithQuery() {
	    class Example {
	      @GET("/foo/bar/?hi=mom") //
	      String method() {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class);
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("hi", "mom"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithPathAndQueryParam() {
	    class Example {
	      @GET("/foo/bar/{ping}/") //
	      String method(@Path("ping") String ping, @Query("kit") String kit,
	          @Query("riff") String riff) {
	        return null;
	      }
	    }

	    IHttpRequest request = buildRequest(Example.class, "pong", "kat", "raff");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/pong/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("kit", "kat"), nameValuePair("riff", "raff"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithPathAndQueryQuestionMarkParam() {
	    class Example {
	      @GET("/foo/bar/{ping}/") //
	      String method(@Path("ping") String ping, @Query("kit") String kit) {
	        return null;
	      }
	    }

	    IHttpRequest request = buildRequest(Example.class, "pong?", "kat?");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/pong%3F/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("kit", "kat?"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithPathAndQueryAmpersandParam() {
	    class Example {
	      @GET("/foo/bar/{ping}/") //
	      String method(@Path("ping") String ping, @Query("kit") String kit) {
	        return null;
	      }
	    }

	    IHttpRequest request = buildRequest(Example.class, "pong&", "kat&");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/pong%26/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("kit", "kat&"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithPathAndQueryHashParam() {
	    class Example {
	      @GET("/foo/bar/{ping}/") //
	      String method(@Path("ping") String ping, @Query("kit") String kit) {
	        return null;
	      }
	    }

	    IHttpRequest request = buildRequest(Example.class, "pong#", "kat#");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/pong%23/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("kit", "kat#"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithQueryParamList() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Query("key") List<Object> keys) {
	        return null;
	      }
	    }

	    List<Object> values = Arrays.<Object>asList(1, 2, null, "three");
	    IHttpRequest request = buildRequest(Example.class, values);
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    //assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/foo/bar/?key=1&key=2&key=three");
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("key", "1"), nameValuePair("key", "2"), nameValuePair("key", "three"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithQueryParamArray() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Query("key") Object[] keys) {
	        return null;
	      }
	    }

	    Object[] values = { 1, 2, null, "three" };
	    IHttpRequest request = buildRequest(Example.class, new Object[] { values });
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("key", "1"), nameValuePair("key","2"), nameValuePair("key","three"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithQueryParamPrimitiveArray() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Query("key") int[] keys) {
	        return null;
	      }
	    }

	    int[] values = { 1, 2, 3 };
	    IHttpRequest request = buildRequest(Example.class, new Object[] { values });
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("key", "1"), nameValuePair("key","2"), nameValuePair("key","3"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithQueryParamMap() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@QueryMap Map<String, Object> query) {
	        return null;
	      }
	    }

	    Map<String, Object> params = new LinkedHashMap<String, Object>();
	    params.put("kit", "kat");
	    params.put("foo", null);
	    params.put("ping", "pong");

	    IHttpRequest request = buildRequest(Example.class, params);
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("kit", "kat"), nameValuePair("ping", "pong"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithEncodedQueryParamMap() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@QueryMap(encodeValues = false) Map<String, Object> query) {
	        return null;
	      }
	    }

	    Map<String, Object> params = new LinkedHashMap<String, Object>();
	    params.put("kit", "k%20t");
	    params.put("foo", null);
	    params.put("ping", "p%20g");

	    IHttpRequest request = buildRequest(Example.class, params);
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("kit", "k t"), nameValuePair("ping", "p g"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithEncodeNameQueryParamMap() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@QueryMap(encodeNames = true) Map<String, Object> query) {
	        return null;
	      }
	    }

	    Map<String, Object> params = new LinkedHashMap<String, Object>();
	    params.put("k it", "k t");
	    params.put("fo o", null);
	    params.put("pi ng", "p g");

	    IHttpRequest request = buildRequest(Example.class, params);
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("k it", "k t"), nameValuePair("pi ng", "p g"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void getWithEncodeNameEncodedValueQueryParamMap() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(
	          @QueryMap(encodeNames = true, encodeValues = false) Map<String, Object> query) {
	        return null;
	      }
	    }

	    Map<String, Object> params = new LinkedHashMap<String, Object>();
	    params.put("k it", "k%20t");
	    params.put("fo o", null);
	    params.put("pi ng", "p%20g");

	    IHttpRequest request = buildRequest(Example.class, params);
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("k it", "k t"), nameValuePair("pi ng","p g"));
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void normalPostWithPathParam() {
	    class Example {
	      @POST("/foo/bar/{ping}/") //
	      String method(@Path("ping") String ping, @Body Object body) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "pong", new Object());
	    assertThat(request.getHttpMethod()).isEqualTo("POST");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/pong/");
	    assertBody(request.getContent(), "{}");
	  }

	  @Test public void bodyGson() {
	    class Example {
	      @POST("/foo/bar/") //
	      String method(@Body Object body) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, Arrays.asList("quick", "brown", "fox"));
	    assertThat(request.getHttpMethod()).isEqualTo("POST");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertBody(request.getContent(), "[\"quick\",\"brown\",\"fox\"]");
	  }

	  @Test public void bodyResponseBody() {
	    class Example {
	      @POST("/foo/bar/") //
	      String method(@Body InputStream body) {
	        return null;
	      }
	    }
	    //RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "hi");
	    InputStream body = new ByteArrayInputStream( "hi".getBytes() );
	    IHttpRequest request = buildRequest(Example.class, body);
	    assertThat(request.getHttpMethod()).isEqualTo("POST");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertBody(request.getContent(), "hi");
	  }

	  @Test public void bodyRequired() {
	    class Example {
	      @POST("/foo/bar/") //
	      String method(@Body InputStream body) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class, new Object[] { null });
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e.getMessage()).isEqualTo("Body parameter value must not be null.");
	    }
	  }

	  @Test public void bodyWithPathParams() {
	    class Example {
	      @POST("/foo/bar/{ping}/{kit}/") //
	      String method(@Path("ping") String ping, @Body Object body, @Path("kit") String kit) {
	        return null;
	      }
	    }
	    IHttpRequest request =
	        buildRequest(Example.class, "pong", Arrays.asList("quick", "brown", "fox"), "kat");
	    assertThat(request.getHttpMethod()).isEqualTo("POST");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/pong/kat/");
	    assertBody(request.getContent(), "[\"quick\",\"brown\",\"fox\"]");
	  }
/*
	  @Test public void simpleMultipart() throws IOException {
	    class Example {
	      @Multipart //
	      @POST("/foo/bar/") //
	      String method(@Part("ping") String ping, @Part("kit") ResponseBody kit) {
	        return null;
	      }
	    }

	    IHttpRequest request = buildRequest(Example.class, "pong", RequestBody.create(
	        MediaType.parse("text/plain"), "kat"));
	    assertThat(request.getHttpMethod()).isEqualTo("POST");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/foo/bar/");

	    RequestBody body = request.getContent();
	    Buffer buffer = new Buffer();
	    body.writeTo(buffer);
	    String bodyString = buffer.readUtf8();

	    assertThat(bodyString)
	        .contains("name=\"ping\"\r\n")
	        .contains("\r\npong\r\n--");

	    assertThat(bodyString)
	        .contains("name=\"kit\"")
	        .contains("\r\nkat\r\n--");
	  }

	  @Test public void multipartWithEncoding() throws IOException {
	    class Example {
	      @Multipart //
	      @POST("/foo/bar/") //
	      String method(@Part(value = "ping", encoding = "8-bit") String ping,
	          @Part(value = "kit", encoding = "7-bit") ResponseBody kit) {
	        return null;
	      }
	    }

	    IHttpRequest request = buildRequest(Example.class, "pong", RequestBody.create(
	        MediaType.parse("text/plain"), "kat"));
	    assertThat(request.getHttpMethod()).isEqualTo("POST");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/foo/bar/");

	    RequestBody body = request.getContent();
	    Buffer buffer = new Buffer();
	    body.writeTo(buffer);
	    String bodyString = buffer.readUtf8();

	    assertThat(bodyString).contains("name=\"ping\"\r\n")
	        .contains("Content-Transfer-Encoding: 8-bit")
	        .contains("\r\npong\r\n--");

	    assertThat(bodyString).contains("name=\"kit\"")
	        .contains("Content-Transfer-Encoding: 7-bit")
	        .contains("\r\nkat\r\n--");
	  }

	  @Test public void multipartPartMap() throws IOException {
	    class Example {
	      @Multipart //
	      @POST("/foo/bar/") //
	      String method(@PartMap Map<String, Object> parts) {
	        return null;
	      }
	    }

	    Map<String, Object> params = new LinkedHashMap<String, Object>();
	    params.put("ping", "pong");
	    params.put("kit", RequestBody.create(MediaType.parse("text/plain"), "kat"));

	    IHttpRequest request = buildRequest(Example.class, params);
	    assertThat(request.getHttpMethod()).isEqualTo("POST");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/foo/bar/");

	    RequestBody body = request.getContent();
	    Buffer buffer = new Buffer();
	    body.writeTo(buffer);
	    String bodyString = buffer.readUtf8();

	    assertThat(bodyString)
	        .contains("name=\"ping\"\r\n")
	        .contains("\r\npong\r\n--");

	    assertThat(bodyString)
	        .contains("name=\"kit\"")
	        .contains("\r\nkat\r\n--");
	  }

	  @Test public void multipartPartMapWithEncoding() throws IOException {
	    class Example {
	      @Multipart //
	      @POST("/foo/bar/") //
	      String method(@PartMap(encoding = "8-bit") Map<String, Object> parts) {
	        return null;
	      }
	    }

	    Map<String, Object> params = new LinkedHashMap<String, Object>();
	    params.put("ping", "pong");
	    params.put("kit", RequestBody.create(MediaType.parse("text/plain"), "kat"));

	    IHttpRequest request = buildRequest(Example.class, params);
	    assertThat(request.getHttpMethod()).isEqualTo("POST");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/foo/bar/");

	    RequestBody body = request.getContent();
	    Buffer buffer = new Buffer();
	    body.writeTo(buffer);
	    String bodyString = buffer.readUtf8();

	    assertThat(bodyString).contains("name=\"ping\"\r\n")
	        .contains("Content-Transfer-Encoding: 8-bit")
	        .contains("\r\npong\r\n--");

	    assertThat(bodyString).contains("name=\"kit\"")
	        .contains("Content-Transfer-Encoding: 8-bit")
	        .contains("\r\nkat\r\n--");
	  }

	  @Test public void multipartPartMapRejectsNullKeys() {
	    class Example {
	      @Multipart //
	      @POST("/foo/bar/") //
	      String method(@PartMap Map<String, Object> parts) {
	        return null;
	      }
	    }

	    Map<String, Object> params = new LinkedHashMap<String, Object>();
	    params.put("ping", "pong");
	    params.put(null, "kat");

	    try {
	      buildRequest(Example.class, params);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Parameter #1 part map contained null key.");
	    }
	  }

	  @Test public void multipartNullRemovesPart() throws IOException {
	    class Example {
	      @Multipart //
	      @POST("/foo/bar/") //
	      String method(@Part("ping") String ping, @Part("fizz") String fizz) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "pong", null);
	    assertThat(request.getHttpMethod()).isEqualTo("POST");
	    assertThat(request.getHeaders().size()).isZero();
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/foo/bar/");

	    RequestBody body = request.getContent();
	    Buffer buffer = new Buffer();
	    body.writeTo(buffer);
	    String bodyString = buffer.readUtf8();

	    assertThat(bodyString)
	        .contains("name=\"ping\"")
	        .contains("\r\npong\r\n--");
	  }

	  @Test public void multipartPartOptional() {
	    class Example {
	      @Multipart //
	      @POST("/foo/bar/") //
	      String method(@Part("ping") RequestBody ping) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class, new Object[] { null });
	      fail();
	    } catch (IllegalStateException e) {
	      assertThat(e.getMessage()).isEqualTo("Multipart body must have at least one part.");
	    }
	  }
	  */
	  @Test public void simpleFormEncoded() {
	    class Example {
	      @FormUrlEncoded //
	      @POST("/foo") //
	      String method(@Field("foo") String foo, @Field("ping") String ping) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "bar", "pong");
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("foo", "bar"), nameValuePair("ping", "pong"));
	  }

	  @Test public void formEncodedWithEncodedNameFieldParam() {
	    class Example {
	      @FormUrlEncoded //
	      @POST("/foo") //
	      String method(@Field(value = "na%20me", encode = false) String foo) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "ba%20r");
	    // See addFormField() method in RequestBuilder for explanations on expected value "ba r".
	    assertThat(request.getParameters()).containsOnly(nameValuePair("na%20me", "ba r"));
	  }

	  @Test public void formEncodedFieldOptional() {
	    class Example {
	      @FormUrlEncoded //
	      @POST("/foo") //
	      String method(@Field("foo") String foo, @Field("ping") String ping,
	          @Field("kit") String kit) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "bar", null, "kat");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("foo", "bar"), nameValuePair("kit", "kat"));
	  }

	  @Test public void formEncodedFieldList() {
	    class Example {
	      @FormUrlEncoded //
	      @POST("/foo") //
	      String method(@Field("foo") List<Object> fields, @Field("kit") String kit) {
	        return null;
	      }
	    }

	    List<Object> values = Arrays.<Object>asList("foo", "bar", null, 3);
	    IHttpRequest request = buildRequest(Example.class, values, "kat");
	    //assertBody(request.getContent(), "foo=foo&foo=bar&foo=3&kit=kat");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("foo", "foo"), nameValuePair("foo", "bar"), nameValuePair("foo", "3"), nameValuePair("kit", "kat"));
	  }

	  @Test public void formEncodedFieldArray() {
	    class Example {
	      @FormUrlEncoded //
	      @POST("/foo") //
	      String method(@Field("foo") Object[] fields, @Field("kit") String kit) {
	        return null;
	      }
	    }

	    Object[] values = { 1, 2, null, "three" };
	    IHttpRequest request = buildRequest(Example.class, values, "kat");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("foo", "1"), nameValuePair("foo", "2"), nameValuePair("foo", "three"), nameValuePair("kit", "kat"));
	    //assertBody(request.getContent(), "foo=1&foo=2&foo=three&kit=kat");
	  }

	  @Test public void formEncodedFieldPrimitiveArray() {
	    class Example {
	      @FormUrlEncoded //
	      @POST("/foo") //
	      String method(@Field("foo") int[] fields, @Field("kit") String kit) {
	        return null;
	      }
	    }

	    int[] values = { 1, 2, 3 };
	    IHttpRequest request = buildRequest(Example.class, values, "kat");
	    //assertBody(request.getContent(), "foo=1&foo=2&foo=3&kit=kat");
	    assertThat(request.getParameters()).containsOnly(nameValuePair("foo", "1"), nameValuePair("foo", "2"), nameValuePair("foo", "3"), nameValuePair("kit", "kat"));
	    
	  }

	  @Test public void formEncodedWithEncodedNameFieldParamMap() {
	    class Example {
	      @FormUrlEncoded //
	      @POST("/foo") //
	      String method(@FieldMap(encode = false) Map<String, Object> fieldMap) {
	        return null;
	      }
	    }

	    Map<String, Object> fieldMap = new LinkedHashMap<String, Object>();
	    fieldMap.put("k%20it", "k%20at");
	    fieldMap.put("pin%20g", "po%20ng");

	    IHttpRequest request = buildRequest(Example.class, fieldMap);
	    assertThat(request.getParameters()).containsOnly(nameValuePair("k%20it", "k at"), nameValuePair("pin%20g", "po ng"));
	  }

	  @Test public void formEncodedFieldMap() {
	    class Example {
	      @FormUrlEncoded //
	      @POST("/foo") //
	      String method(@FieldMap Map<String, Object> fieldMap) {
	        return null;
	      }
	    }

	    Map<String, Object> fieldMap = new LinkedHashMap<String, Object>();
	    fieldMap.put("kit", "kat");
	    fieldMap.put("foo", null);
	    fieldMap.put("ping", "pong");

	    IHttpRequest request = buildRequest(Example.class, fieldMap);
	    assertThat(request.getParameters()).containsOnly(nameValuePair("kit", "kat"), nameValuePair("ping", "pong"));
	  }

	  @Test public void fieldMapRejectsNullKeys() {
	    class Example {
	      @FormUrlEncoded //
	      @POST("/") //
	      String method(@FieldMap Map<String, Object> a) {
	        return null;
	      }
	    }

	    Map<String, Object> fieldMap = new LinkedHashMap<String, Object>();
	    fieldMap.put("kit", "kat");
	    fieldMap.put("foo", null);
	    fieldMap.put(null, "pong");

	    try {
	      buildRequest(Example.class, fieldMap);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Parameter #1 field map contained null key.");
	    }
	  }

	  @Test public void fieldMapMustBeAMap() {
	    class Example {
	      @FormUrlEncoded //
	      @POST("/") //
	      String method(@FieldMap List<String> a) {
	        return null;
	      }
	    }
	    try {
	      buildRequest(Example.class);
	      fail();
	    } catch (IllegalArgumentException e) {
	      assertThat(e).hasMessage("Example.method: @FieldMap parameter type must be Map. (parameter #1)");
	    }
	  }

	  @Test public void simpleHeaders() {
	    class Example {
	      @GET("/foo/bar/")
	      @com.github.lpezet.antiope2.retrofitted.annotation.http.Headers({
	          "ping: pong",
	          "kit: kat"
	      })
	      String method() {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class);
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isEqualTo(2);
	    assertThat(request.getFirstHeaderValue("ping")).isEqualTo("pong");
	    assertThat(request.getFirstHeaderValue("kit")).isEqualTo("kat");
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void headerParamToString() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Header("kit") BigInteger kit) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, new BigInteger("1234"));
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isEqualTo(1);
	    assertThat(request.getFirstHeaderValue("kit")).isEqualTo("1234");
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getContent()).isNull();
	  }

	  @Test public void headerParam() {
	    class Example {
	      @GET("/foo/bar/") //
	      @com.github.lpezet.antiope2.retrofitted.annotation.http.Headers("ping: pong") //
	      String method(@Header("kit") String kit) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, "kat");
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    assertThat(request.getHeaders().size()).isEqualTo(2);
	    assertThat(request.getFirstHeaderValue("ping")).isEqualTo("pong");
	    assertThat(request.getFirstHeaderValue("kit")).isEqualTo("kat");
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/");
	    assertThat(request.getResourcePath()).isEqualTo("/foo/bar/");
	    assertThat(request.getContent()).isNull();
	  }

	//TODO: Need to fix Antiope to either accommodate for more than 1 value for same header in addHeader() (can just use comma to separate values) or have headers be Map<String, List<String>>.
	  /*
	  @Test public void headerParamList() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Header("foo") List<String> kit) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, Arrays.asList("bar", null, "baz"));
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    Map<String, String> headers = request.getHeaders();
	    assertThat(headers.size()).isEqualTo(2);
	    assertThat(headers.values("foo")).containsExactly("bar", "baz");
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/foo/bar/");
	    assertThat(request.getContent()).isNull();
	  }

	  
	  @Test public void headerParamArray() {
	    class Example {
	      @GET("/foo/bar/") //
	      String method(@Header("foo") String[] kit) {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class, (Object) new String[] { "bar", null, "baz" });
	    assertThat(request.getHttpMethod()).isEqualTo("GET");
	    Map<String, String> headers = request.getHeaders();
	    assertThat(headers.size()).isEqualTo(2);
	    assertThat(headers.values("foo")).containsExactly("bar", "baz");
	    assertThat(request.getEndpoint().toString()).isEqualTo("http://example.com/foo/bar/");
	    assertThat(request.getContent()).isNull();
	  }
	*/
	  @Test public void contentTypeAnnotationHeaderOverrides() {
	    class Example {
	      @POST("/") //
	      @com.github.lpezet.antiope2.retrofitted.annotation.http.Headers("Content-Type: text/not-plain") //
	      String method(@Body InputStream body) {
	        return null;
	      }
	    }
	    //RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "hi");
	    InputStream body = new ByteArrayInputStream( "hi".getBytes() );
	    IHttpRequest request = buildRequest(Example.class, body);
	    assertThat(request.getFirstHeaderValue("Content-Type")).isEqualTo("text/not-plain");
	  }

	  @Test public void contentTypeAnnotationHeaderAddsHeaderWithNoBody() {
	    class Example {
	      @DELETE("/") //
	      @com.github.lpezet.antiope2.retrofitted.annotation.http.Headers("Content-Type: text/not-plain") //
	      String method() {
	        return null;
	      }
	    }
	    IHttpRequest request = buildRequest(Example.class);
	    assertThat(request.getFirstHeaderValue("Content-Type")).isEqualTo("text/not-plain");
	  }

	  @Test public void contentTypeParameterHeaderOverrides() {
	    class Example {
	      @POST("/") //
	      String method(@Header("Content-Type") String contentType, @Body InputStream body) {
	        return null;
	      }
	    }
	    //RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "Plain");
	    InputStream body = new ByteArrayInputStream( "abc".getBytes() );
	    IHttpRequest request = buildRequest(Example.class, "text/not-plain", body);
	    assertThat(request.getFirstHeaderValue("Content-Type")).isEqualTo("text/not-plain");
	  }

	  private static void assertBody(InputStream body, String expected) {
	    assertThat(body).isNotNull();
	    try {
	    	String oBody = IOUtils.toString( body );
	    	assertThat(oBody).isEqualTo(expected);
	    } catch (IOException e) {
	      throw new RuntimeException(e);
	    }
	  }

	  private static final Converter GSON = new GsonConverter(new Gson());

	  private IHttpRequest buildRequest(Class<?> cls, Object... args) {
	    Method method = TestingUtils.onlyMethod(cls);
	    MethodInfo methodInfo = new MethodInfo(method);

	    RequestBuilder builder = new RequestBuilder("http://example.com/", methodInfo, GSON);
	    builder.setArguments(args);
	    try {
			return builder.build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	  }
}
