component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	// LDEV-6675 the built-in HTTP engine follows redirects like Apache HttpClient did before:
	// redirect=true follows GET and HEAD (and a 303 always), redirect=lax also POST and DELETE but not PUT or PATCH,
	// without pooling Apache HttpClient always followed the lax way.
	// A 301/302 turns the request into a GET, a 307/308 keeps the method and the body.

	variables.httpbin = server.getTestService( "httpbin" );

	private boolean function noHttpbin() {
		return structCount( variables.httpbin ) == 0;
	}

	private string function httpbinURL( required string path ) {
		return "http://#variables.httpbin.server#:#variables.httpbin.port#/#arguments.path#";
	}

	private struct function call( required string method, required numeric status, string redirect="true", boolean pooling=true ) {
		var target = urlEncodedFormat( httpbinURL( "anything" ) );
		var u = httpbinURL( "redirect-to?url=#target#&status_code=#arguments.status#" );
		http url=u method=arguments.method redirect=arguments.redirect pooling=arguments.pooling result="local.res" throwOnError=false {
			if ( listFindNoCase( "POST,PUT,PATCH,DELETE", arguments.method ) ) httpparam type="body" value="ldev6675";
		}
		return res;
	}

	private void function expectFollowed( required struct res, required string method, string body="" ) {
		expect( res.status_code ).toBe( 200 );
		var data = deserializeJSON( res.fileContent );
		expect( data.method ).toBe( arguments.method );
		if ( len( arguments.body ) ) expect( data.data ).toBe( arguments.body );
		expect( res.locations ).toBeArray();
		expect( len( res.locations ) ).toBe( 1 );
	}

	private void function expectNotFollowed( required struct res, required numeric status ) {
		expect( res.status_code ).toBe( arguments.status );
		expect( res ).notToHaveKey( "locations" );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6675 redirects like Apache HttpClient", skip=noHttpbin(), body=function() {

			it( "redirect=true follows GET", function() {
				expectFollowed( call( "GET", 302 ), "GET" );
			});

			it( "redirect=true with pooling does not follow a POST 302", function() {
				expectNotFollowed( call( "POST", 302 ), 302 );
			});

			it( "redirect=true with pooling does not follow a POST 307", function() {
				expectNotFollowed( call( "POST", 307 ), 307 );
			});

			it( "redirect=true always follows a 303, as GET", function() {
				expectFollowed( call( "POST", 303 ), "GET" );
			});

			it( "redirect=true without pooling follows a POST 302 the lax way, as GET", function() {
				expectFollowed( call( "POST", 302, "true", false ), "GET" );
			});

			it( "redirect=true without pooling follows a POST 307 with method and body", function() {
				expectFollowed( call( "POST", 307, "true", false ), "POST", "ldev6675" );
			});

			it( "redirect=lax follows a POST 302 as GET", function() {
				expectFollowed( call( "POST", 302, "lax" ), "GET" );
			});

			it( "redirect=lax follows a POST 307 with method and body", function() {
				expectFollowed( call( "POST", 307, "lax" ), "POST", "ldev6675" );
			});

			it( "redirect=lax follows a DELETE 302", function() {
				expectFollowed( call( "DELETE", 302, "lax" ), "GET" );
			});

			it( "redirect=lax does not follow a PUT 302", function() {
				expectNotFollowed( call( "PUT", 302, "lax" ), 302 );
			});

			it( "redirect=lax does not follow a PATCH 307", function() {
				expectNotFollowed( call( "PATCH", 307, "lax" ), 307 );
			});

			it( "redirect=false follows nothing", function() {
				expectNotFollowed( call( "GET", 302, "false" ), 302 );
			});
		});
	}
}
