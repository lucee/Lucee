component extends="org.lucee.cfml.test.LuceeTestCase" labels="http,admin" javaSettings='{
		"maven": [
			"com.squareup.okhttp3:mockwebserver:4.12.0"
		]
	}' {

	import "okhttp3.mockwebserver.MockWebServer";
	import "okhttp3.mockwebserver.MockResponse";

	// the Administrator functions are tested from the source, the admin itself doesn't need to be installed
	variables.adminSrc = getDirectoryFromPath( getCurrentTemplatePath() ) & "../../core/src/main/cfml/context/admin/";

	function beforeAll() {
		variables.mock = new MockWebServer();
		variables.mock.start();
		application action="update" mappings={ "/ldev7002admin": variables.adminSrc };
		include "/ldev7002admin/ext.functions.cfm";
	}

	function afterAll() {
		if ( !isNull( variables.mock ) ) variables.mock.shutdown();
		structDelete( application, "providerInfoListings" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-7002 Lucee version in the User-Agent and conditional requests for the extension provider listing", function() {

			it( title="the user agent for provider calls contains the Lucee version", body=function( currentSpec ) {
				expect( providerUserAgent() ).toBe( "Lucee/" & server.lucee.version & " (CFML Engine)" );
				var ua = createObject( "java", "lucee.runtime.config.Constants" ).getUserAgent();
				expect( ua ).toBe( "Lucee/" & server.lucee.version & " (CFML Engine)" );
			});

			it( title="an unchanged listing is not transferred again", body=function( currentSpec ) {
				var uri = providerUri( "a" );
				var body = '{"meta":{"title":"mock"},"extensions":{"COLUMNS":[],"DATA":[]}}';

				// first load: no If-None-Match, listing kept with its ETag
				enqueue( 200, body, 'W/"listing-1"' );
				var rsp = providerInfoRequest( uri );
				expect( rsp.status_code ).toBe( 200 );
				expect( rsp.fileContent ).toBe( body );
				var req = takeRequest();
				expect( req.getHeader( "User-Agent" ) ).toBe( providerUserAgent() );
				expect( req.getHeader( "Accept" ) ).toInclude( "application/json" );
				expect( isNull( req.getHeader( "If-None-Match" ) ) ).toBeTrue();
				expect( req.getPath() ).toInclude( "coreVersion=" & server.lucee.version );

				// within 5 minutes the kept listing is used without a request
				var count = variables.mock.getRequestCount();
				rsp = providerInfoRequest( uri );
				expect( rsp.status_code ).toBe( 200 );
				expect( rsp.fileContent ).toBe( body );
				expect( variables.mock.getRequestCount() ).toBe( count );

				// after that the ETag is sent and a 304 reuses the kept listing
				expireKeptListings();
				enqueue( 304, "", 'W/"listing-1"' );
				rsp = providerInfoRequest( uri );
				req = takeRequest();
				expect( req.getHeader( "If-None-Match" ) ).toBe( 'W/"listing-1"' );
				expect( rsp.status_code ).toBe( 200 );
				expect( rsp.notModified ).toBeTrue();
				expect( rsp.fileContent ).toBe( body );
			});

			it( title="a changed listing replaces the kept one", body=function( currentSpec ) {
				var uri = providerUri( "b" );
				enqueue( 200, '{"v":1}', 'W/"v1"' );
				providerInfoRequest( uri );
				takeRequest();

				expireKeptListings();
				enqueue( 200, '{"v":2}', 'W/"v2"' );
				var rsp = providerInfoRequest( uri );
				expect( takeRequest().getHeader( "If-None-Match" ) ).toBe( 'W/"v1"' );
				expect( rsp.fileContent ).toBe( '{"v":2}' );

				expireKeptListings();
				enqueue( 304, "", 'W/"v2"' );
				rsp = providerInfoRequest( uri );
				expect( takeRequest().getHeader( "If-None-Match" ) ).toBe( 'W/"v2"' );
				expect( rsp.fileContent ).toBe( '{"v":2}' );
			});

			it( title="a provider without ETag works as before", body=function( currentSpec ) {
				var uri = providerUri( "c" );
				var kept = structCount( application.providerInfoListings ?: {} );
				enqueue( 200, '{"x":1}', "" );
				var rsp = providerInfoRequest( uri );
				expect( rsp.fileContent ).toBe( '{"x":1}' );
				expect( isNull( takeRequest().getHeader( "If-None-Match" ) ) ).toBeTrue();
				// nothing is kept, so the next load is a plain request again
				expect( structCount( application.providerInfoListings ) ).toBe( kept );
			});

			it( title="an error response is passed on", body=function( currentSpec ) {
				var uri = providerUri( "d" );
				enqueue( 500, "failed", "" );
				var rsp = providerInfoRequest( uri );
				expect( rsp.status_code ).toBe( 500 );
				takeRequest();
			});
		});
	}

	private string function providerUri( required string name ) {
		return variables.mock.url( "/" & arguments.name & "/" & createUUID() & "/rest/extension/provider/info" ).toString();
	}

	private void function enqueue( required numeric status, required string body, required string etag ) {
		var rsp = new MockResponse().setResponseCode( arguments.status ).setBody( arguments.body );
		if ( len( arguments.etag ) ) rsp = rsp.setHeader( "ETag", arguments.etag );
		if ( arguments.status == 200 ) rsp = rsp.setHeader( "Content-Type", "application/json" );
		variables.mock.enqueue( rsp );
	}

	private function takeRequest() {
		var req = variables.mock.takeRequest( 5, createObject( "java", "java.util.concurrent.TimeUnit" ).SECONDS );
		expect( isNull( req ) ).toBeFalse( "no request reached the mock provider" );
		return req;
	}

	private void function expireKeptListings() {
		loop collection=application.providerInfoListings key="local.k" value="local.listing" {
			listing.loaded = dateAdd( "n", -10, now() );
		}
	}
}
