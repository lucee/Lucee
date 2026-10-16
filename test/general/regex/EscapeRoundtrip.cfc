// REEscape round-trip: REEscape(x) used as a regex pattern matches x literally.
// Covers punctuation, special regex metacharacters, alphanumerics, and spaces.
// Both engines escape consistently per the LDEV-2892 / LDEV-4310 sample dict.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	public void function testAlphanumericsUnescaped() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "abc123", reEscape( "abc123" ) );
			assertEquals( [ "abc123" ], reMatch( reEscape( "abc123" ), "abc123" ) );
		});
	}

	public void function testEmptyString() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "", reEscape( "" ) );
		});
	}

	public void function testSpaceEscaped() {
		variables._regex.eachEngine( function( engine ) {
			// space is non-letterOrDigit, so escaped
			assertEquals( "\ ", reEscape( " " ) );
			assertEquals( [ " " ], reMatch( reEscape( " " ), " " ) );
		});
	}

	public void function testRegexMetacharsEscaped() {
		variables._regex.eachEngine( function( engine ) {
			var literal = "Hello. How are you? (test) [123] {abc} ^$.*+?|\";
			var pattern = reEscape( literal );
			assertEquals( [ literal ], reMatch( pattern, literal ) );
		});
	}

	public void function testForwardSlashEscaped() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "\/s\/acf\/lucee", reEscape( "/s/acf/lucee" ) );
		});
	}

	public void function testQuestionBracketCaret() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "lucee\?\[\]\^", reEscape( "lucee?[]^" ) );
		});
	}

	public void function testRoundTripWithBackrefsLiteral() {
		variables._regex.eachEngine( function( engine ) {
			// "\Qabc\E" — perl/java differ on whether Q/E are escaped, but
			// the round-trip property holds: pattern matches the original literal
			var literal = "\Qabc\E";
			assertEquals( [ literal ], reMatch( reEscape( literal ), literal ) );
		});
	}
}
