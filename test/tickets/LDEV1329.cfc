component extends="org.lucee.cfml.test.LuceeTestCase" labels="array,struct" {

	function run( testResults, testBox ) {
		describe( "LDEV-1329 a[1] = x on an undefined variable creates an array", function() {

			it( title="unscoped", body=function( currentSpec ) {
				var r = _run( "unscoped.cfm" );
				expect( r ).toBe( "array|1|x" );
			});

			it( title="variables scope", body=function( currentSpec ) {
				expect( _run( "variables.cfm" ) ).toBe( "array|1|x" );
			});

			it( title="local scope and var", body=function( currentSpec ) {
				expect( _run( "local.cfm" ) ).toBe( "array,array|1|x" );
			});

			it( title="nested arrays and struct inside an array", body=function( currentSpec ) {
				expect( _run( "nested.cfm" ) ).toBe( "array|array|a-b|array|struct|5" );
			});

			it( title="a member below an existing struct", body=function( currentSpec ) {
				expect( _run( "member.cfm" ) ).toBe( "struct|array|x" );
			});

			it( title="struct keys are not affected", body=function( currentSpec ) {
				expect( _run( "struct.cfm" ) ).toBe( "struct|struct|struct|x|y|z" );
			});

			it( title="existing arrays and structs are not replaced", body=function( currentSpec ) {
				expect( _run( "existing.cfm" ) ).toBe( "array|3|struct|1,A" );
			});

			it( title="inside a function", body=function( currentSpec ) {
				expect( _run( "function.cfm" ) ).toBe( "array,array|1" );
			});
		});
	}

	private string function _run( required string template ) {
		return trim( _InternalRequest( template: createURI( "LDEV1329/#arguments.template#" ) ).filecontent );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
