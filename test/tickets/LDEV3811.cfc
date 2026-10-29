component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI( "LDEV3811" );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-3811: debugging state is tracked per request, not only in the config", function() {

			it( "records no method call entries when debugging is not enabled", function() {
				var data = runRequest( {} );
				expect( data.positional ).toBe( 3 );
				expect( data.named ).toBe( 7 );
				expect( data.hasDebugData ).toBeFalse();
				expect( methodEntries( data ) ).toBeEmpty();
			} );

			it( "records method call entries when debuggingTemplate is set in Application.cfc", function() {
				var data = runRequest( { appDebug: true } );
				expect( data.positional ).toBe( 3 );
				expect( data.named ).toBe( 7 );
				expect( data.hasDebugData ).toBeTrue();
				expect( methodEntries( data ) ).toHaveLength( 1 );
			} );

			it( "records method call entries when debuggingTemplate is set with cfapplication action=update", function() {
				var data = runRequest( { requestDebug: true } );
				expect( data.positional ).toBe( 3 );
				expect( data.named ).toBe( 7 );
				expect( data.hasDebugData ).toBeTrue();
				expect( methodEntries( data ) ).toHaveLength( 1 );
			} );

		} );
	}

	private struct function runRequest( required struct urls ) {
		var result = _InternalRequest(
			template: "#variables.uri#/test.cfm",
			urls: arguments.urls
		);
		return deserializeJSON( trim( result.filecontent ) );
	}

	private array function methodEntries( required struct data ) {
		return arrayFilter( data.srcs, function( src ) {
			return reFindNoCase( "test\.cfm\$add$", src ) > 0;
		} );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
