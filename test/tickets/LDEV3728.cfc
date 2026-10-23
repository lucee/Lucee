component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	// a redirect chain of our own, update.lucee.org now redirects more than once and that changes over time
	variables.httpbin = server.getTestService( "httpbin" );

	private boolean function noHttpbin() {
		return structCount( variables.httpbin ) == 0;
	}

	private string function httpbinURL( required string path ) {
		return "http://#variables.httpbin.server#:#variables.httpbin.port#/#arguments.path#";
	}

	function beforeAll(){
		if ( noHttpbin() ) return;
		variables.target = httpbinURL( "get" );
		variables.testUrl = httpbinURL( "redirect-to?url=#urlEncodedFormat( variables.target )#&status_code=302" ); // one 302 to /get
	}

	function run( testResults, testBox ) {
		describe( title="Testcase for LDEV-3728", skip=noHttpbin(), body=function() {
			it( title="Checking cfhttp returns redirect locations, with redirect=true", body=function( currentSpec ) {
				http url=variables.testUrl result="local.res" redirect=true;
				expect( res ).toHaveKey( "locations" );
				expect( res.locations ).toBeArray();
				expect( len( res.locations ) ).toBe( 1 );
				expect( res.locations[ 1 ] ).toBe( variables.target );
				expect( res.status_code ).toBe( 200 );
			});

			it( title="Checking cfhttp returns every redirect location of a chain", body=function( currentSpec ) {
				http url=httpbinURL( "redirect/3" ) result="local.res" redirect=true;
				expect( res.locations ).toBeArray();
				expect( len( res.locations ) ).toBe( 3 );
				expect( res.locations[ 3 ] ).toBe( httpbinURL( "get" ) );
				expect( res.status_code ).toBe( 200 );
			});

			it( title="Checking cfhttp doesn't return redirect locations, redirect=false", body=function( currentSpec ) {
				http url=variables.testUrl result="local.res" redirect=false;
				expect( res ).notToHaveKey( "locations" );
				expect( res.status_code ).toBe( 302 );
			});
		});
	}

}
