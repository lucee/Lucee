component extends="org.lucee.cfml.test.LuceeTestCase" labels="hash" {

	function run( testResults, testBox ) {
		describe( "LDEV-4154 hash() with algorithm quick and numIterations", function() {

			it( title="throws for numIterations > 1", body=function( currentSpec ) {
				expect( function() {
					hash( "x", "quick", "utf-8", 2 );
				}).toThrow( regex="numIterations" );
			});

			it( title="throws for a large numIterations", body=function( currentSpec ) {
				expect( function() {
					hash( "x", "quick", "utf-8", 1000 );
				}).toThrow( regex="numIterations" );
			});

			it( title="works with numIterations=1", body=function( currentSpec ) {
				var h = hash( "x", "quick", "utf-8", 1 );
				expect( len( h ) ).toBe( 16 );
				expect( h ).toBe( hash( "x", "quick" ) );
			});

			it( title="works with numIterations=1 as a named argument", body=function( currentSpec ) {
				expect( hash( input="x", algorithm="quick", numIterations=1 ) ).toBe( hash( "x", "quick" ) );
			});

			it( title="numIterations > 1 still works for other algorithms", body=function( currentSpec ) {
				var h = hash( "x", "md5", "utf-8", 2 );
				expect( h ).notToBe( hash( "x", "md5", "utf-8", 1 ) );
			});

		});
	}

}
