// Dot-vs-newline semantics. Lucee's perl/Oro engine treats `.` as matching
// every character including \n and \r (documented Lucee/ACF exception to
// standard Perl). Java/compat defaults to dot-NOT-newline; (?s) lifts the
// restriction. Pins the regression LDEV-2156 surfaced — compat should match
// perl behaviour once the pattern translator lands.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	// === default dot-matches-newline behaviour (perl semantics) ===

	public void function testDotMatchesLineFeed() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, arrayLen( reMatch( ".", chr(10) ) ) );
		});
	}

	public void function testDotMatchesCarriageReturn() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, arrayLen( reMatch( ".", chr(13) ) ) );
		});
	}

	public void function testDotMatchesCRLF() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			// two chars, dot matches each individually
			assertEquals( 2, arrayLen( reMatch( ".", chr(13) & chr(10) ) ) );
		});
	}

	public void function testDotConsumesNewlineInsideQuantifier() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			// `a.+d` consumes the embedded \n in perl — whole input matches as one run
			var input = "ab" & chr(10) & "cd";
			assertEquals( [ input ], reMatch( "a.+d", input ) );
		});
	}

	public void function testDotMatchesNewlineInReFind() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "a.b", "a" & chr(10) & "b" ) );
		});
	}

	public void function testDotMatchesNewlineInReReplace() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			// replace `a.b` with `X` — newline gets eaten with surrounding chars
			assertEquals( "X", reReplace( "a" & chr(10) & "b", "a.b", "X" ) );
		});
	}


	// === (?s) inline DOTALL — lifts restriction on every engine ===

	public void function testDotallFlagMatchesLineFeed() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, arrayLen( reMatch( "(?s).", chr(10) ) ) );
		});
	}

	public void function testDotallFlagConsumesAcrossLines() {
		variables._regex.eachEngine( function( engine ) {
			var input = "ab" & chr(10) & "cd";
			assertEquals( [ input ], reMatch( "(?s)a.+d", input ) );
		});
	}


	// === sanity baseline: dot matches every non-newline char regardless of engine ===

	public void function testDotMatchesSpace() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "a b" ], reMatch( "a.b", "a b" ) );
		});
	}

	public void function testDotMatchesPunct() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "a@b" ], reMatch( "a.b", "a@b" ) );
		});
	}

	public void function testDotMatchesDigit() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "a5b" ], reMatch( "a.b", "a5b" ) );
		});
	}


	// === character-class equivalents always work on every engine ===

	public void function testCharClassNewlineExplicit() {
		variables._regex.eachEngine( function( engine ) {
			// [\s\S] is the engine-portable "match any character including newline"
			var input = "ab" & chr(10) & "cd";
			assertEquals( [ input ], reMatch( "a[\s\S]+d", input ) );
		});
	}
}
