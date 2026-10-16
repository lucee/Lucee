component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-2417", function() {
			it( title="fat arrow function with a single parameter without parentheses", body=function( currentSpec ) {
				expect( runTemplate( "single.cfm" ) ).toBe( "42" );
			});
			it( title="fat arrow function with a single parameter without parentheses as argument", body=function( currentSpec ) {
				expect( runTemplate( "members.cfm" ) ).toBe( "100,200,300|200,300|600" );
			});
			it( title="fat arrow function with a single parameter without parentheses and a body, nested", body=function( currentSpec ) {
				expect( runTemplate( "block.cfm" ) ).toBe( "Hello Lucee|3" );
			});
			it( title="comparison operators are unchanged", body=function( currentSpec ) {
				var x = 2;
				expect( x >= 1 ).toBeTrue();
				expect( x == 2 ).toBeTrue();
			});
		});
	}
	private string function runTemplate( required string name ) {
		try {
			return trim( _internalRequest( template: "#createURI( "LDEV2417" )#/#arguments.name#" ).filecontent );
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
