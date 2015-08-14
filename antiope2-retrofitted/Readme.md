# Antiope2 Retrofitted (A2R)

Antiope2 Retrofitted turns API into Java interfaces, just like Retrofit does :)

	public interface GitHubService {
		@GET("/users/{user}/repos")
		List<Repo> listRepos(@Path("user") String user);
	}
	
The `RestAdapter` class generates an implementation of the GitHubService interface.

	RestAdapter restAdapter = new RestAdapter.Builder()
    .setEndpoint("https://api.github.com")
    .build();
    
    GitHubService service = restAdapter.create(GitHubService.class);
   
Each call on the generated GitHubService makes an HTTP request to the remote webserver.

	List<Repo> repos = service.listRepos("octocat");
	
Use annotations to describe the HTTP request:

* URL parameter replacement and query parameter support
* Object conversion to request body (e.g., JSON, protocol buffers)
* (Not yet supported) Multipart request body and file upload


# Differences with Retrofit

The goal was not to have yet another Retrofit knockoff but to provide the same ease of coding API clients with [Antiope2](https://github.com/lpezet/antiope/tree/master/antiope2-core).

Retrofit is coupled with OkHttp client and doesn't allow for much customizations (besides the already rich configuration it provides).

 

## @Query

The `@Query` annotation has a `template` attribute for specifying a template for the value of the query parameter.

	@GET("/v1/public/yql?format=json")
	public ForecastResponse forecast(@Query(value="q", template="select item from weather.forecast where location=\"{}\"") String pLocation);
	
See Antiope2-Samples project for more samples.

## @Converter

With the `@Converter` annotation, it's possible to specify method-level converters.

	@GET("/v1/public/yql?format=xml")
	@Converter(YahooRSSConverter.class)
	public ForecastResponse forecast(@Query(value="q", template="select item from weather.forecast where location=\"{}\"") String pLocation);
	
## Client

Antiope2 Retrofitted can support more than just OkHttp client. A2R relies on the `IHttpNetworkIO` interface to serialize domain objects back and forth the Http wire.


# More to come


## Custom annotations

Support for adding custom annotations and be processed by `RestAdapter`.

	@Retry(exceptions=ArrayIndexOutOfBoundsException.class, maxExecutions=1)
	@GET("/ping")
	String ping()


Or even full support for custom HTTP annotations (e.g. custom methods)

	@CUSTOM("/ping")
	String ping()
	

## Multipart Support

