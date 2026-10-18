component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	function beforeAll() {
		variables.httpbin = server.getTestService( "httpbin" );
	}

	function isDisabled() {
		return structCount( server.getTestService( "httpbin" ) ) == 0;
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-5726: http resources with characters that need URL encoding", skip=isDisabled(), body=function() {

			it( title="fileExists() with a space in the path", body=function( currentSpec ) {
				expect( fileExists( baseUrl() & "/anything/my file.txt" ) ).toBeTrue();
			} );

			it( title="fileExists() with an already encoded path", body=function( currentSpec ) {
				expect( fileExists( baseUrl() & "/anything/my%20file.txt" ) ).toBeTrue();
			} );

			it( title="fileExists() with non-ASCII and bracket characters in the path", body=function( currentSpec ) {
				expect( fileExists( baseUrl() & "/anything/über [1].txt" ) ).toBeTrue();
			} );

			it( title="fileRead() requests the right path", body=function( currentSpec ) {
				var result = deserializeJSON( fileRead( baseUrl() & "/anything/my file.txt" ) );
				// httpbin may echo the url encoded or decoded
				expect( urlDecode( result.url ) ).toInclude( "/anything/my file.txt" );
			} );

		} );
	}

	private string function baseUrl() {
		return "http://#variables.httpbin.server#:#variables.httpbin.port#";
	}

}
