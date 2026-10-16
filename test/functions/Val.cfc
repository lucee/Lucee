component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "val() BIF", function() {

			it( "extracts number from start of string", function() {
				expect( val( "1234 Main St." ) ).toBe( 1234 );
			});

			it( "returns 0 when string doesn't start with number", function() {
				expect( val( "Main St., 1234" ) ).toBe( 0 );
			});

			it( "handles decimal numbers", function() {
				expect( val( "123.456" ) ).toBe( 123.456 );
			});

			it( "returns 0 for empty string", function() {
				expect( val( "" ) ).toBe( 0 );
			});

			it( "returns 0 for non-numeric string", function() {
				expect( val( "a" ) ).toBe( 0 );
				expect( val( "one" ) ).toBe( 0 );
			});

			it( "handles single digit", function() {
				expect( val( "1" ) ).toBe( 1 );
			});

			it( "stops at non-numeric character", function() {
				expect( val( "123T456" ) ).toBe( 123 );
			});

			it( "handles edge cases with decimals", function() {
				expect( val( "0.F" ) ).toBe( 0 );
				expect( val( ".F" ) ).toBe( 0 );
				expect( val( "1.F" ) ).toBe( 1 );
			});

		});

		describe( "val() member function", function() {

			it( "extracts number from start of string", function() {
				expect( "1234 Main St.".val() ).toBe( 1234 );
			});

			it( "returns 0 when string doesn't start with number", function() {
				expect( "Main St., 1234".val() ).toBe( 0 );
			});

			it( "handles decimal numbers", function() {
				expect( "123.456".val() ).toBe( 123.456 );
			});

			it( "returns 0 for empty string", function() {
				expect( "".val() ).toBe( 0 );
			});

			it( "returns 0 for non-numeric string", function() {
				expect( "a".val() ).toBe( 0 );
				expect( "one".val() ).toBe( 0 );
			});

			it( "handles single digit", function() {
				expect( "1".val() ).toBe( 1 );
			});

			it( "stops at non-numeric character", function() {
				expect( "123T456".val() ).toBe( 123 );
			});

			it( "handles edge cases with decimals", function() {
				expect( "0.F".val() ).toBe( 0 );
				expect( ".F".val() ).toBe( 0 );
				expect( "1.F".val() ).toBe( 1 );
			});

		});

	}

}
