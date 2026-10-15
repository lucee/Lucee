component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-4286", function() {
			it( title="anonymous function as a statement inside a function", body=function( currentSpec ) {
				expect( runTemplate( "nested.cfm" ) ).toBe( "ok" );
			});
			it( title="closure and lambda as statements inside a function and outside", body=function( currentSpec ) {
				expect( runTemplate( "other.cfm" ) ).toBe( "ok" );
			});
		});
	}
	private string function runTemplate( required string name ) {
		try {
			return trim( _internalRequest( template: "#createURI( "LDEV4286" )#/#arguments.name#" ).filecontent );
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
