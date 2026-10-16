// Lookaround: (?=...) (?!...) (?<=...) (?<!...).
// All unsupported by the perl engine (Oro) — engine throws when used. Bedrock
// pins them to the java engine only. perl coverage of the "throws" path lives
// in test/tickets/LDEV2892.cfc.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	public void function testPositiveLookahead() unsupportedRegexEngine="perl" {
		variables._regex.eachEngine( function( engine ) {
			// "foo" only when followed by "bar"
			assertEquals( [ "foo" ], reMatch( "foo(?=bar)", "foobar foobaz" ) );
		});
	}

	public void function testNegativeLookahead() unsupportedRegexEngine="perl" {
		variables._regex.eachEngine( function( engine ) {
			// "foo" only when NOT followed by "bar"
			assertEquals( [ "foo" ], reMatch( "foo(?!bar)", "foobar foobaz" ) );
		});
	}

	public void function testPositiveLookbehind() unsupportedRegexEngine="perl" {
		variables._regex.eachEngine( function( engine ) {
			// "pajamas" when preceded by "cat's "
			var r = reMatch( "(?<=cat's )pajamas", "You are the cat's pajamas!" );
			assertEquals( [ "pajamas" ], r );
		});
	}

	public void function testNegativeLookbehind() unsupportedRegexEngine="perl" {
		variables._regex.eachEngine( function( engine ) {
			// digit when NOT preceded by $ — matches the 5 in "id5" but not in "$5"
			var r = reMatch( "(?<!\$)\d", "id5 cost $5" );
			assertEquals( [ "5" ], r );
		});
	}
}
