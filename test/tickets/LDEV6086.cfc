component extends="org.lucee.cfml.test.LuceeTestCase" labels="internalRequest" {

	function beforeAll() {
		variables.uri = createURI( "LDEV6086" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6086", function() {

			it( title="InternalRequest should not swallow RequestTimeoutException", body=function() {
				var result = _InternalRequest(
					template: "#variables.uri#/timeout.cfm",
					throwonerror: false
				);
				expect( result ).toHaveKey( "error" );
				expect( result.error.message ).toInclude( "timeout" );
			});

			it( title="InternalRequest should throw with throwonerror=true on RequestTimeoutException", body=function() {
				try {
					_InternalRequest(
						template: "#variables.uri#/timeout.cfm",
						throwonerror: true
					);
					fail( "expected exception was not thrown" );
				}
				catch ( application e ) {
					// wrapped in ApplicationException so it doesn't kill the parent request
					expect( e.message ).toInclude( "timeout" );
				}
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
