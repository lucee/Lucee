component extends="org.lucee.cfml.test.LuceeTestCase" labels="math" {

	function run( testResults, testBox ) {
		describe( "LDEV-25 randRange() must stay inside the given range", function() {

			it( "does not overflow when the max is the max int", function() {
				var max = 2 ^ 31 - 1;
				for ( var i = 1; i <= 100; i++ ) {
					expect( randRange( 0, max ) ).toBeBetween( 0, max );
				}
			});

			it( "does not overflow when the range spans the whole int range", function() {
				var min = -( 2 ^ 31 );
				var max = 2 ^ 31 - 1;
				for ( var i = 1; i <= 100; i++ ) {
					expect( randRange( min, max ) ).toBeBetween( min, max );
				}
			});

			it( "supports values beyond the int range", function() {
				var min = 2 ^ 40;
				var max = 2 ^ 41;
				for ( var i = 1; i <= 100; i++ ) {
					expect( randRange( min, max ) ).toBeBetween( min, max );
				}
			});

			it( "accepts the arguments in reversed order", function() {
				for ( var i = 1; i <= 100; i++ ) {
					expect( randRange( 10, 5 ) ).toBeBetween( 5, 10 );
				}
			});

			it( "returns the value if min and max are equal", function() {
				expect( randRange( 7, 7 ) ).toBe( 7 );
			});

		});
	}

}
