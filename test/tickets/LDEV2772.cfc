component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		application action="update" scopeCascading="standard";
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2772", function() {

			// note scopeCascading has no effect currently

			it( title = "enable/disable search result, scopeCascading=standard", body = function( currentSpec ) {
				application action="update" scopeCascading="standard";
				checkSearchResults();
			});

			it( title = "enable/disable search result, scopeCascading=strict", body = function( currentSpec ) {
				application action="update" scopeCascading="strict";
				checkSearchResults();
				application action="update" scopeCascading="standard";
			});

			it( title = "enable/disable search result, scopeCascading=small", body = function( currentSpec ) {
				application action="update" scopeCascading="small";
				checkSearchResults();
				application action="update" scopeCascading="standard";
			});
		});
	}

	function afterAll(){
		application action="update" scopeCascading="standard";
		application action="update" searchResults=true;
	}

	private function checkSearchResults(){
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
		}
		finally {
			application action="update" searchResults=true;
		}
	}
}