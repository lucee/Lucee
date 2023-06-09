component extends = "org.lucee.cfml.test.LuceeTestCase" skip=false {
	function run( testResults , testBox ) {
		describe( title = "Test for === operator", body = function() {

			it( title = 'same type (string)',body = function( currentSpec ) localmode=true {

				s1 = "ABC";
				s2 = chr(65) & chr(66) & chr(67);// this is necessary because Java internalize all literal strings

				expect ( s1 == s2 ).toBeTrue( "== 2 differen strings with the same value" );
				expect ( s1 === s2 ).toBeTrue( "=== 2 differen strings with the same value" );
			});


			it( title = 'different types (double|BigDecimal ans string)',body = function( currentSpec ) localmode=true {

				s1 = "1";
				s2 = 1;

				expect ( s1 == s2 ).toBeTrue( "== 2 differen types but same value" );
				expect ( s1 === s2 ).toBeFalse( "=== 2 differen types but same value" );
			});


			it( title = 'Test case for === operator with strings',body = function( currentSpec ) localmode=true {

				a = "lucee";
				b = "lucee";
				c = "Lucee";
				d = duplicate(a);

				expect ( a === d ).toBeTrue( "compare same values, variables" );
				expect ( a === b ).toBeTrue( "compare duplicated value, variables" );
				expect( "lucee" === "lucee").toBeTrue( "compare same values, inline" );

				// ACF compat, differs to JS, cfml is case insensitive
				expect ( a === c ).toBeTrue( "compare same values, different case" );
				expect( "lucee" === "Lucee").toBeTrue( "compare same values, different case, inline" );
			});

			it( title = 'Test case for === operator with numbers',body = function( currentSpec ) localmode=true {

				a = 1;
				b = 1;
				c = 2;
				d = duplicate( a );

				expect ( a === d ).toBeTrue( "compare same values, variables" );
				expect ( a === b ).toBeTrue( "compare duplicated value, variables" );
				expect( 1 === 1).toBeTrue( "compare same values, inline" );

				expect ( a === c ).toBeFalse( "compare same values, different case, variables" );
				expect( 1 === 2).toBeFalse( "compare same values, inline" );
			});

			it( title = 'Test case for === operator with numbers as strings',body = function( currentSpec ) localmode=true {

				a = 1;
				b = "1";

				expect ( a === b ).toBeFalse( "compare string and number, variables" );
				expect( 1 === "1").toBeFalse( "compare string and number, inline" );

				expect ( a == b ).toBeTrue( "traditional compare string and number, variables" );
				expect ( 1 == "1" ).toBeTrue( "traditional compare string and number, inline" );
			});

		});
	}
}