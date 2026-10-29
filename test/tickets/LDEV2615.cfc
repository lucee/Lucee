component extends="org.lucee.cfml.test.LuceeTestCase" labels="validate" {

	function run( testResults, testBox ) {
		describe( "LDEV-2615 isValid(""creditcard"") with the MasterCard 2-series BIN range", function() {

			it( title="accepts 2-series MasterCard numbers (2221-2720)", body=function() {
				expect( isValid( "creditcard", "2223000048400011" ) ).toBeTrue();
				// lower and upper boundary of the range
				expect( isValid( "creditcard", "2221000000000009" ) ).toBeTrue();
				expect( isValid( "creditcard", "2720999999999996" ) ).toBeTrue();
			});

			it( title="rejects numbers just outside the 2-series range", body=function() {
				expect( isValid( "creditcard", "2220999999999991" ) ).toBeFalse();
				expect( isValid( "creditcard", "2721000000000004" ) ).toBeFalse();
			});

			it( title="rejects a 2-series number with a bad check digit", body=function() {
				expect( isValid( "creditcard", "2223000048400012" ) ).toBeFalse();
			});

			it( title="still accepts the classic 51-55 MasterCard range and Visa", body=function() {
				expect( isValid( "creditcard", "5555555555554444" ) ).toBeTrue();
				expect( isValid( "creditcard", "4111111111111111" ) ).toBeTrue();
			});

		});
	}

}
