component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		application action="update" scopeCascading="standard";
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2772", function() {
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
		var q = query( a:[1], b:[2] );
		var b = 3;
				
		try {
			application action="update" searchResults=false;
			loop query="q" {
				expect( isNull(a) ).toBeTrue();
				expect( q.a ).toBe( 1 );
				expect( q.b ).toBe( 2 );
				expect( b ).toBe( 3 ); // queries get checked before local scope
			}

			application action="update" searchQueries=true;
			loop query="q" {
				expect( isNull( a ) ).toBeFalse();
				expect( q.a ).toBe( 1 );
				expect( q.b ).toBe( 2 );
				expect( b ).toBe( 3 ); // queries get checked before local scope
			}
		}
		finally {
			application action="update" searchResults=true;
		}
	}
}