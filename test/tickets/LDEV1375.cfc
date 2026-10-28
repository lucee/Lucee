component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache" {

	function beforeAll() {
		variables.uri = createURI( "LDEV1375" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1375: cfcache action=clientcache does not output its body", function() {

			it( title="cfcache action=clientcache outputs the body of the tag", body=function( currentSpec ) {
				var result = _InternalRequest( template: "#variables.uri#/test.cfm" );
				expect( result.filecontent.trim() ).toBe( '<div id="id">HelloWorld</div>' );
			});

		});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
