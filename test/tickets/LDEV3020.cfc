component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-3020", function() {
			it( title="compound integer division (\=)", body=function( currentSpec ) {
				expect( runTemplate( "intdiv.cfm" ) ).toBe( "3,3" );
			});
			it( title="compound integer division (\=) inside an if without curly brackets", body=function( currentSpec ) {
				expect( runTemplate( "if.cfm" ) ).toBe( "0,4" );
			});
			it( title="precisionEvaluate with the exponent operator (^)", body=function( currentSpec ) {
				expect( runTemplate( "precision.cfm" ) ).toBe( "81,9,70.63" );
			});
			it( title="isNull with safe navigation", body=function( currentSpec ) {
				expect( runTemplate( "safenav.cfm" ) ).toBe( "true,true,true" );
			});
		});
	}
	private string function runTemplate( required string name ) {
		try {
			return trim( _internalRequest( template: "#createURI( "LDEV3020" )#/#arguments.name#" ).filecontent );
		}
		catch ( any e ) {
			return e.message;
		}
	}
	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
