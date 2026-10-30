component extends="org.lucee.cfml.test.LuceeTestCase" labels="operator" {

	function run( testResults, testBox ) {
		describe( "LDEV-31 IMP operator short circuits", function() {

			it( "does not evaluate the right side when the left side is false", function() {
				var calls = 0;
				var probe = function() { calls++; return true; };
				var result = ( false imp probe() );
				expect( result ).toBeTrue();
				expect( calls ).toBe( 0 );
			});

			it( "evaluates the right side when the left side is true", function() {
				var calls = 0;
				var probe = function() { calls++; return false; };
				var result = ( true imp probe() );
				expect( result ).toBeFalse();
				expect( calls ).toBe( 1 );
			});

			it( "keeps the truth table", function() {
				expect( true imp true ).toBeTrue();
				expect( true imp false ).toBeFalse();
				expect( false imp true ).toBeTrue();
				expect( false imp false ).toBeTrue();
			});

			it( "keeps the truth table when evaluated dynamically", function() {
				expect( evaluate( "true imp true" ) ).toBeTrue();
				expect( evaluate( "true imp false" ) ).toBeFalse();
				expect( evaluate( "false imp true" ) ).toBeTrue();
				expect( evaluate( "false imp false" ) ).toBeTrue();
			});

			it( "does not evaluate the right side dynamically when the left side is false", function() {
				var calls = 0;
				var probe = function() { calls++; return true; };
				var result = evaluate( "false imp probe()" );
				expect( result ).toBeTrue();
				expect( calls ).toBe( 0 );
			});

		});
	}

}
