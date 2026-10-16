component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-4286", function() {
			it( title="closure written as a statement inside a function throws a compile error", body=function( currentSpec ) {
				var msg = runTemplate( "nested.cfm" );
				expect( msg ).toInclude( "Invalid construct" );
				expect( msg ).toInclude( "closure written as a statement is never used" );
			});
			it( title="lambda written as a statement throws a compile error", body=function( currentSpec ) {
				var msg = runTemplate( "lambda.cfm" );
				expect( msg ).toInclude( "Invalid construct" );
				expect( msg ).toInclude( "lambda written as a statement is never used" );
			});
			it( title="closure written as a statement outside a function throws a compile error", body=function( currentSpec ) {
				var msg = runTemplate( "toplevel.cfm" );
				expect( msg ).toInclude( "Invalid construct" );
				expect( msg ).toInclude( "closure written as a statement is never used" );
			});
			it( title="assigned, passed and invoked closures and lambdas and named functions still work", body=function( currentSpec ) {
				expect( runTemplate( "valid.cfm" ) ).toBe( "4;6;8;10;12;2,3;7|14" );
			});
			it( title="closure statement inside a string of a script component is not a compile error", body=function( currentSpec ) {
				expect( new LDEV4286.ScriptInString().getCode() ).toInclude( "function(){" );
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
