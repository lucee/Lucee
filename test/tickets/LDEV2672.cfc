component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-2672", function() {
			it( title="Elvis operator with a literal as left operand", body=function( currentSpec ) {
				expect( runTemplate( "literal.cfm" ) ).toBe( "42,true," );
			});
			it( title="Elvis operator with an expression as left operand", body=function( currentSpec ) {
				expect( runTemplate( "expression.cfm" ) ).toBe( "2,LUCEE!,default" );
			});
			it( title="Elvis operator with a variable as left operand is unchanged", body=function( currentSpec ) {
				var s = { a: 1 };
				expect( s.a ?: "default" ).toBe( 1 );
				expect( s.b ?: "default" ).toBe( "default" );
				expect( nullValue() ?: "default" ).toBe( "default" );
			});
		});
	}
	private string function runTemplate( required string name ) {
		try {
			return trim( _internalRequest( template: "#createURI( "LDEV2672" )#/#arguments.name#" ).filecontent );
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
