component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		application action="update" scopeCascading="standard";
		variables.uri = createURI("LDEV2772");
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2772", function() {

			// note scopeCascading has no effect currently

			it( title = "enable/disable search result, scopeCascading=standard", body = function( currentSpec ) {
				checkSearchResults(scopeCascading="standard");
			});

			it( title = "enable/disable search result, scopeCascading=strict", body = function( currentSpec ) {
				checkSearchResults(scopeCascading="strict");
			});

			it( title = "enable/disable search result, scopeCascading=small", body = function( currentSpec ) {
				checkSearchResults(scopeCascading="small");
			});
		});
	}

	function afterAll(){
		application action="update" scopeCascading="standard";
		application action="update" searchResults=true;
	}

	private function checkSearchResults(string scopeCascading){
		application action="update" scopeCascading="#arguments.scopeCascading#";
		// mostly duplicated in LDEV2772/searchResults.cfm
		var q = query( a:['query'], b:['query'], c:['query']);
		var b = 'local';
		variables.c = 'variables';

		try {
			application action="update" searchResults=false;
			loop query="q" {
				expect( isNull(a) ).toBeTrue();
				expect( q.a ).toBe( 'query' );
				expect( q.b ).toBe( 'query' );
				expect( b ).toBe( 'local' ); // query scope lookup disabled, so local scope
				expect( c ).toBe( 'variables' ); // query scope lookup disabled, so variables scope
			}

			application action="update" searchQueries=true;
			loop query="q" {
				expect( isNull( a ) ).toBeFalse();
				expect( q.a ).toBe( 'query' );
				expect( q.b ).toBe( 'query' );
				// local scope gets checked first, when inside a function with an arguments scope, but in a plain old .cfm template
				expect( b ).toBe( 'local' ); // inside a function, so local scope always gets checked first
				expect( c ).toBe( 'query' ); // query scope get checked before variables scope
			}

			// scope cascading rules are different in a .cfm file
			// there's no local scope and the variables scope is checked after queries
			result = _InternalRequest(
				template : "#uri#/searchResults.cfm",
				url : { scopeCascading: arguments.scopeCascading }
			);
			expect( trim( result.filecontent ) ).toBe( "" );
		}
		finally {
			application action="update" scopeCascading="standard";
			application action="update" searchResults=true;
		}
	}

	private string function createURI( string calledName ){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}