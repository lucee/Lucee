// Perl-style case modifiers in replacement: \u \l \U \E \L.
// Today these are a perl-engine feature; java raw passes them through as
// literal backslash sequences. LDEV-1353 / LDEV-6432 translator adds support
// under java compat=true. Bedrock pins perl behaviour now; raw java is
// excluded via unsupportedRegexEngine="java" and the future java-compat matrix
// entry will run these once the translator lands.
// Supersedes the broken/skipped test/tickets/_LDEV1353.cfc.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	public void function testUpperFirstCharOfGroup() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "Hello", reReplace( "hello", "(.+)", "\u\1" ) );
		});
	}

	public void function testLowerFirstCharOfGroup() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "hELLO", reReplace( "HELLO", "(.+)", "\l\1" ) );
		});
	}

	public void function testUpperGroupTerminatedByE() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "HELLOworld", reReplace( "helloworld", "(hello)(world)", "\U\1\E\2" ) );
		});
	}

	public void function testLowerGroupTerminatedByE() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "helloWORLD", reReplace( "HELLOWORLD", "(hello)(world)", "\L\1\E\2" ) );
		});
	}

	public void function testUnterminatedUpperCascadesToEndOfReplacement() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// \U without \E uppercases through to end of replacement
			assertEquals( "HELLO WORLD", reReplace( "hello world", "(.+)", "\U\1" ) );
		});
	}

	public void function testAdobeWorkedExample() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// from Adobe docs example for \L\1 — full lowercase of captured group
			assertEquals(
				"Don't shout\scream hello",
				reReplace( "HELLO", "([[:upper:]]*)", "Don't shout\scream \L\1" )
			);
		});
	}

	// one-shot \u resets on $, so the char after $ is not upcased
	public void function testOneShotModeClearedByDollarSign() unsupportedRegexEngine="java,perl" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "$hello", reReplace( "hello", "hello", "\u$hello" ) );
		});
	}

	// all-upper \U does not reset on $, so chars after $ continue to be upcased
	public void function testAllUpperModePersistsThroughDollarSign() unsupportedRegexEngine="java,perl" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "$HELLO", reReplace( "hello", "hello", "\U$hello" ) );
		});
	}

	// \u/\l applied to literal text (no group ref) — only the immediately-following char is cased
	public void function testUpperFirstCharOfLiteral() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "Hello world", reReplace( "hello world", "hello", "\uhello" ) );
		});
	}

	public void function testLowerFirstCharOfLiteral() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "hELLO world", reReplace( "HELLO world", "HELLO", "\lHELLO" ) );
		});
	}

	// \\u in replacement: Adobe docs say escape \u with \\ to produce a literal \u in output.
	// All three engines diverge: perl eats the u; compat doesn't collapse \\; only java is correct.
	public void function testEscapedCaseModifierBecomesLiteral() {
		variables._regex.eachEngine( function( engine ) {
			var r = reReplace( "hello", "hello", "\\u" );
			if      ( engine == "perl"   ) assertEquals( "\",   r ); // Oro eats the u after \\
			else if ( engine == "compat" ) assertEquals( "\\u", r ); // expandReplacement doesn't collapse \\
			else                           assertEquals( "\u",  r ); // java: translateReplacement correct
		});
	}
}
