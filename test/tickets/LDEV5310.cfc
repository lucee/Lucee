component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title="LDEV-5310 no space between a script tag and its opening bracket", body=function() {

			it( title="silent{ with no space before the bracket", body=function( currentSpec ) {
				expect( runTemplate( "silent_no_space.cfm" ) ).toBe( "success" );
			});

			it( title="silent{ with no space before the bracket and on one line", body=function( currentSpec ) {
				expect( runTemplate( "silent_no_space_compact.cfm" ) ).toBe( "success" );
			});

			it( title="silent{ nested and inside if, function and try", body=function( currentSpec ) {
				expect( runTemplate( "silent_no_space_nested.cfm" ) ).toBe( "expected-ok-success" );
			});

			it( title="silent with a space, tab, line feed or comment before the bracket still works", body=function( currentSpec ) {
				expect( runTemplate( "silent_variants.cfm" ) ).toBe( "success" );
			});

			it( title="a variable or function starting with silent is not the silent tag", body=function( currentSpec ) {
				expect( runTemplate( "silent_identifiers.cfm" ) ).toBe( "123456" );
			});

			it( title="other script tags with a body and no space before the bracket", body=function( currentSpec ) {
				expect( runTemplate( "block_tags_no_space.cfm" ) ).toBe( "lock,lock2,transaction,transaction2,threadtrue,savecontent,stopwatch,loop,loop,loop1,loop2,silent" );
			});

		});
	}

	private string function runTemplate( required string template ) {
		var result = _InternalRequest( template: createURI( "LDEV5310/" & arguments.template ) );
		if ( result.status != 200 ) throw( message="status [#result.status#] for [#arguments.template#]", detail=result.filecontent );
		return trim( result.filecontent );
	}

	private string function createURI( string calledName ) {
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		return contractPath( base ) & "/" & arguments.calledName;
	}

}
