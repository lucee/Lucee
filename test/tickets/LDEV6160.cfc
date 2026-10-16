component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi,encoding" {

	function run( testResults, testBox ) {
		describe( "LDEV-6160", function() {

			it( "serializeJson correctly escapes strings containing newlines and special chars", function() {
				var test = "a {" & chr( 13 ) & chr( 10 ) & "	text-decoration: none;" & chr( 13 ) & chr( 10 ) & "}";
				var json = serializeJson( test );
				// must be valid JSON
				var roundtrip = deserializeJson( json );
				expect( roundtrip ).toBe( test );
			});

			it( "encodeForJavascript must escape newlines and carriage returns", function() {
				var test = "a {" & chr( 13 ) & chr( 10 ) & "	text-decoration: none;" & chr( 13 ) & chr( 10 ) & "}";
				var encoded = encodeForJavascript( test );
				// raw newlines in a JS string literal are a syntax error
				expect( encoded ).notToInclude( chr( 13 ) );
				expect( encoded ).notToInclude( chr( 10 ) );
			});

		});
	}

}
