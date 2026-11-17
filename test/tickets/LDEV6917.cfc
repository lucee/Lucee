component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6917 replaceNoCase() with U+0130 and U+00DF in the same string", function() {

			it( title="replaces the right range when the input has both characters", body=function() {
				expect( replaceNoCase( chr( 304 ) & chr( 223 ) & " abc", "ABC", "X" ) ).toBe( chr( 304 ) & chr( 223 ) & " X" );
			});

			it( title="still works with only one of the two characters", body=function() {
				expect( replaceNoCase( chr( 304 ) & " abc", "ABC", "X" ) ).toBe( chr( 304 ) & " X" );
				expect( replaceNoCase( chr( 223 ) & " abc", "ABC", "X" ) ).toBe( chr( 223 ) & " X" );
			});

			it( title="replaces all and only the first occurrence", body=function() {
				var input = chr( 304 ) & chr( 223 ) & " abc ABC aBc";
				expect( replaceNoCase( input, "abc", "X", "all" ) ).toBe( chr( 304 ) & chr( 223 ) & " X X X" );
				expect( replaceNoCase( input, "abc", "X" ) ).toBe( chr( 304 ) & chr( 223 ) & " X ABC aBc" );
			});

			it( title="does not leave characters of the matched text behind", body=function() {
				var prefix = chr( 304 ) & "stanbul, Goethe-Stra" & chr( 223 ) & "e ";
				var result = replaceNoCase( prefix & "<!-- $$location.main$$ --> x", "<!-- $$LOCATION.main$$ -->", "MAIN", "all" );
				expect( result ).toBe( prefix & "MAIN x" );
			});

			it( title="the struct variant gives the same result", body=function() {
				var input = chr( 304 ) & chr( 223 ) & " abc def";
				expect( replaceNoCase( input, { "ABC": "X", "DEF": "Y" } ) ).toBe( chr( 304 ) & chr( 223 ) & " X Y" );
			});

		});
	}

}
