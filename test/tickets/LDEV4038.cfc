component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-4038 _internalRequest template relative to the calling template", function() {

			it( title="a path without a leading slash is relative to the calling template", body=function( currentSpec ) {
				expect( _internalRequest( template: "LDEV4038/index.cfm" ).filecontent.trim() ).toBe( "LDEV4038-root" );
			});

			it( title="a path starting with ./ is relative to the calling template", body=function( currentSpec ) {
				expect( _internalRequest( template: "./LDEV4038/index.cfm" ).filecontent.trim() ).toBe( "LDEV4038-root" );
			});

			it( title="a relative path can point into a sub directory", body=function( currentSpec ) {
				expect( _internalRequest( template: "LDEV4038/sub/index.cfm" ).filecontent.trim() ).toBe( "LDEV4038-sub" );
				expect( _internalRequest( template: "LDEV4038/sub/deeper/index.cfm" ).filecontent.trim() ).toBe( "LDEV4038-deeper" );
			});

			it( title="a relative path can contain backslashes", body=function( currentSpec ) {
				expect( _internalRequest( template: "LDEV4038\sub\deeper\index.cfm" ).filecontent.trim() ).toBe( "LDEV4038-deeper" );
			});

			it( title="a relative path can step up with ../", body=function( currentSpec ) {
				expect( _internalRequest( template: "../tickets/LDEV4038/index.cfm" ).filecontent.trim() ).toBe( "LDEV4038-root" );
				expect( _internalRequest( template: "LDEV4038/sub/../index.cfm" ).filecontent.trim() ).toBe( "LDEV4038-root" );
				expect( _internalRequest( template: "LDEV4038/sub/deeper/../../sub/index.cfm" ).filecontent.trim() ).toBe( "LDEV4038-sub" );
			});

			it( title="a relative path that steps up beyond the root throws", body=function( currentSpec ) {
				expect( function() {
					_internalRequest( template: "../../../../../../../../../../LDEV4038/index.cfm" );
				}).toThrow( "expression" );
			});

			it( title="a path with a leading slash is used as it is", body=function( currentSpec ) {
				expect( _internalRequest( template: "/test/tickets/LDEV4038/index.cfm" ).filecontent.trim() ).toBe( "LDEV4038-root" );
				expect( _internalRequest( template: createURI( "LDEV4038" ) & "/sub/index.cfm" ).filecontent.trim() ).toBe( "LDEV4038-sub" );
				expect( _internalRequest( template: createURI( "LDEV4038" ) & "\sub\index.cfm" ).filecontent.trim() ).toBe( "LDEV4038-sub" );
			});

			it( title="a root relative path without the leading slash is no longer root relative (breaking change)", body=function( currentSpec ) {
				var res = _internalRequest( template: "test/tickets/LDEV4038/index.cfm", throwonerror: false );
				expect( res.filecontent ).toInclude( "Page [/test/tickets/test/tickets/LDEV4038/index.cfm]" );
				expect( res.status ).toBe( 500 );
			});

			it( title="the path is relative to the template that makes the call, not to the test case", body=function( currentSpec ) {
				expect( _internalRequest( template: "/test/tickets/LDEV4038/caller/call.cfm" ).filecontent.trim() ).toBe( "LDEV4038-sub" );
			});

		});
	}

	private string function createURI( string calledName ) {
		return getDirectoryFromPath( contractPath( getCurrentTemplatePath() ) ) & calledName;
	}

}
