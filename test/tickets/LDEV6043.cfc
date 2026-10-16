component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.preciseMath = getApplicationSettings().preciseMath;
	}

	function afterAll() {
		application action="update" preciseMath=variables.preciseMath;
	}

	function run( testResults, testBox ) {
		describe( title="test case for LDEV-6043", body=function() {

			afterEach( function() {
				application action="update" preciseMath=variables.preciseMath;
			});

			it( "DecimalFormat with large numbers should not use scientific notation", function() {
				expect( DecimalFormat( 10000000 ) ).toBe( "10,000,000.00" );
				expect( DecimalFormat( 20000000 ) ).toBe( "20,000,000.00" );
				expect( DecimalFormat( 100000000 ) ).toBe( "100,000,000.00" );
				expect( DecimalFormat( 1000000000 ) ).toBe( "1,000,000,000.00" );
			});

			it( "DecimalFormat with negative large numbers", function() {
				expect( DecimalFormat( -10000000 ) ).toBe( "-10,000,000.00" );
				expect( DecimalFormat( -20000000 ) ).toBe( "-20,000,000.00" );
			});

			it( "DecimalFormat boundary values around scientific notation threshold", function() {
				expect( DecimalFormat( 9999999 ) ).toBe( "9,999,999.00" );
				expect( DecimalFormat( 19999999 ) ).toBe( "19,999,999.00" );
				expect( DecimalFormat( 20000001 ) ).toBe( "20,000,001.00" );
			});

			it( "DecimalFormat with large numbers and preciseMath=true", function() {
				application action="update" preciseMath=true;
				expect( DecimalFormat( 10000000 ) ).toBe( "10,000,000.00" );
				expect( DecimalFormat( 20000000 ) ).toBe( "20,000,000.00" );
				expect( DecimalFormat( 100000000 ) ).toBe( "100,000,000.00" );
			});

			it( "DecimalFormat with large numbers and preciseMath=false", function() {
				application action="update" preciseMath=false;
				expect( DecimalFormat( 10000000 ) ).toBe( "10,000,000.00" );
				expect( DecimalFormat( 20000000 ) ).toBe( "20,000,000.00" );
				expect( DecimalFormat( 100000000 ) ).toBe( "100,000,000.00" );
			});

		});
	}

}
