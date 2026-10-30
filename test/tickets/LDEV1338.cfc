component extends="org.lucee.cfml.test.LuceeTestCase" labels="render" {

	function run( testResults, testBox ) {
		describe( "LDEV-1338 render() with returnOutput", function() {

			it( title="without the argument the output is written to the response", body=function( currentSpec ) {
				expect( _run( "default.cfm" ) ).toBe( "<b>hi</b>[]|" );
			});

			it( title="returnOutput=false is the same as the default", body=function( currentSpec ) {
				expect( _run( "false.cfm" ) ).toBe( "<b>hi</b>[]|" );
			});

			it( title="returnOutput=true returns the output and writes nothing to the response", body=function( currentSpec ) {
				expect( _run( "true.cfm" ) ).toBe( "<b>hi 3</b>|" );
			});

			it( title="nested output is part of the returned string", body=function( currentSpec ) {
				expect( _run( "nested.cfm" ) ).toBe( "<outer><inner>1</inner><inner>2</inner></outer>|" );
			});

			it( title="an exception in the rendered code cleans up the buffer", body=function( currentSpec ) {
				expect( _run( "exception.cfm" ) ).toBe( "caught|after" );
			});

			it( title="returnOutput as named argument", body=function( currentSpec ) {
				expect( _run( "named.cfm" ) ).toBe( "x" );
			});
		});
	}

	private string function _run( required string template ) {
		return trim( _InternalRequest( template: createURI( "LDEV1338/#arguments.template#" ) ).filecontent );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
