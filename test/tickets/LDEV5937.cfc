component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title="LDEV-5937 this.functionPaths resolves the function template directly", body=function() {

			it( title="finds a function in the first function path", body=function( currentSpec ) {
				expect( call( "onlyInA" ) ).toBe( "A" );
			});

			it( title="finds a function in a later function path", body=function( currentSpec ) {
				expect( call( "onlyInB" ) ).toBe( "B" );
			});

			it( title="the first function path wins when the function exists in several", body=function( currentSpec ) {
				expect( call( "inBoth" ) ).toBe( "A" );
			});

			it( title="matches the function name case insensitive", body=function( currentSpec ) {
				expect( call( "caseDiffers" ) ).toBe( "mixed" );
			});

			it( title="built in functions still work when function paths are set", body=function( currentSpec ) {
				expect( call( "bif" ) ).toBe( "ABC" );
			});

			it( title="only .cfm files are used as functions", body=function( currentSpec ) {
				expect( call( "notATemplate" ) ).toBe( "ERROR:expression:No matching function [notATemplate] found" );
			});

			it( title="an unknown function still throws the same error", body=function( currentSpec ) {
				expect( call( "missing" ) ).toBe( "ERROR:expression:No matching function [thisFunctionDoesNotExist] found" );
			});

		});
	}

	private string function call( required string fn ) {
		var uri = createURI( "LDEV5937" );
		var result = _InternalRequest(
			template: "#uri#/test.cfm",
			url: { fn: arguments.fn }
		);
		return trim( result.filecontent );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
