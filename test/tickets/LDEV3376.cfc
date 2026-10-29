component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	function beforeAll() {
		variables.httpbin = server.getTestService( "httpbin" );
	}

	function isDisabled() {
		return structCount( server.getTestService( "httpbin" ) ) == 0;
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-3376: authentication for the http/https virtual filesystem", skip=isDisabled(), body=function() {

			describe( title="credentials in the path", body=function() {

				it( title="fileRead() sends the credentials as basic authentication", body=function( currentSpec ) {
					var rsp = deserializeJSON( fileRead( urlWith( "susi:sorglos@", "basic-auth/susi/sorglos" ) ) );
					expect( rsp.authenticated ).toBeTrue();
					expect( rsp.user ).toBe( "susi" );
				} );

				it( title="a colon in the password", body=function( currentSpec ) {
					var rsp = deserializeJSON( fileRead( urlWith( "susi:sor:glos@", "basic-auth/susi/sor:glos" ) ) );
					expect( rsp.authenticated ).toBeTrue();
				} );

				it( title="fileExists() is true with the right credentials", body=function( currentSpec ) {
					expect( fileExists( urlWith( "susi:sorglos@", "basic-auth/susi/sorglos" ) ) ).toBeTrue();
				} );

				it( title="fileExists() is false with the wrong credentials", body=function( currentSpec ) {
					expect( fileExists( urlWith( "susi:wrong@", "basic-auth/susi/sorglos" ) ) ).toBeFalse();
				} );

				it( title="fileExists() is false without credentials", body=function( currentSpec ) {
					expect( fileExists( urlWith( "", "basic-auth/susi/sorglos" ) ) ).toBeFalse();
				} );

			} );

			describe( title="this.vfs.http in the Application.cfc", body=function() {

				it( title="username and password are sent as basic authentication, host:port is looked up before host", body=function( currentSpec ) {
					var result = read( urlWith( "", "basic-auth/susi/sorglos" ) );
					expect( result ).notToHaveKey( "error" );
					expect( result.exists ).toBeTrue();
					expect( deserializeJSON( result.content ).authenticated ).toBeTrue();
				} );

				it( title="the credentials of the path win over the Application.cfc", body=function( currentSpec ) {
					var result = read( urlWith( "other:pw@", "basic-auth/other/pw" ) );
					expect( result ).notToHaveKey( "error" );
					expect( deserializeJSON( result.content ).authenticated ).toBeTrue();

					// the wrong credentials in the path are not replaced by the right ones of the Application.cfc
					expect( read( urlWith( "other:wrong@", "basic-auth/susi/sorglos" ) ).exists ).toBeFalse();
				} );

				it( title="an Authorization header is sent", body=function( currentSpec ) {
					var result = read( urlWith( "", "bearer", true ) );
					expect( result ).notToHaveKey( "error" );
					expect( result.exists ).toBeTrue();
					var rsp = deserializeJSON( result.content );
					expect( rsp.authenticated ).toBeTrue();
					expect( rsp.token ).toBe( "secret-token" );
				} );

				it( title="other headers are sent", body=function( currentSpec ) {
					var result = read( urlWith( "", "headers", true ) );
					expect( result ).notToHaveKey( "error" );
					var headers = deserializeJSON( result.content ).headers;
					expect( headers[ "X-LDEV3376" ] ).toBe( "hello" );
					expect( headers[ "Authorization" ] ).toBe( "Bearer secret-token" );
				} );

				it( title="the headers apply to fileExists()", body=function( currentSpec ) {
					expect( read( urlWith( "", "bearer", true ) ).exists ).toBeTrue();
				} );

				it( title="a redirect to the same origin keeps the headers", body=function( currentSpec ) {
					var target = urlWith( "", "headers", true );
					var result = read( urlWith( "", "redirect-to?url=#urlEncodedFormat( target )#&status_code=302", true ) );
					expect( result ).notToHaveKey( "error" );
					expect( deserializeJSON( result.content ).headers[ "Authorization" ] ).toBe( "Bearer secret-token" );
				} );

				it( title="a redirect to another host does not get the headers", body=function( currentSpec ) {
					// the target is the host with the basic authentication entry, it must not get the headers of the alternative host either
					var target = "http://#variables.httpbin.server#:#variables.httpbin.port#/headers";
					var result = read( urlWith( "", "redirect-to?url=#urlEncodedFormat( target )#&status_code=302", true ) );
					expect( result ).notToHaveKey( "error" );
					var headers = deserializeJSON( result.content ).headers;
					expect( headers ).notToHaveKey( "Authorization" );
					expect( headers ).notToHaveKey( "X-LDEV3376" );
				} );

				it( title="an entry that is not a struct fails with a clear message", body=function( currentSpec ) {
					var result = read( "http://invalid-entry.example/test.txt" );
					expect( result ).toHaveKey( "error" );
					expect( result.error ).toInclude( "needs to be a struct" );
				} );

			} );

		} );
	}

	private string function altHost() {
		return variables.httpbin.server == "127.0.0.1" ? "localhost" : "127.0.0.1";
	}

	// asAlt: use the alternative name of the service
	private string function urlWith( required string credentials, required string path, boolean asAlt=false ) {
		var host = arguments.asAlt ? altHost() : variables.httpbin.server;
		return "http://#arguments.credentials##host#:#variables.httpbin.port#/#arguments.path#";
	}

	private struct function read( required string target ) {
		var uri = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/LDEV3376/read.cfm";
		var result = _InternalRequest( template: uri, urls: { target: arguments.target } );
		return deserializeJSON( result.filecontent.trim() );
	}

}
