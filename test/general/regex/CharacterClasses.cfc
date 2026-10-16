// Character classes: [a-z] ranges, POSIX [[:alpha:]], negation [^...], and
// escapes inside classes. Both engines should handle these consistently.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	public void function testLowercaseRange() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc" ], reMatch( "[a-z]+", "abc123ABC" ) );
		});
	}

	public void function testUppercaseRange() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "ABC" ], reMatch( "[A-Z]+", "abc123ABC" ) );
		});
	}

	public void function testDigitRange() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "123" ], reMatch( "[0-9]+", "abc123ABC" ) );
		});
	}

	public void function testCombinedRange() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc123ABC" ], reMatch( "[a-zA-Z0-9]+", "abc123ABC" ) );
		});
	}

	public void function testNegatedClass() {
		variables._regex.eachEngine( function( engine ) {
			// [^a-z]+ matches everything not lowercase
			assertEquals( [ "123" ], reMatch( "[^a-zA-Z]+", "abc123ABC" ) );
		});
	}

	// POSIX bracket classes are perl-engine only. Java regex doesn't recognise
	// `[[:alpha:]]` natively — it parses the outer brackets as a char class
	// containing the literal chars `:alph` and matches accordingly. Java's
	// equivalent is `\p{Alpha}`. The LDEV-6432 translator may add pattern-side
	// translation later; for now bedrock skips java on these.

	public void function testPosixAlpha() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc", "ABC" ], reMatch( "[[:alpha:]]+", "abc 123 ABC" ) );
		});
	}

	public void function testPosixDigit() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "123", "456" ], reMatch( "[[:digit:]]+", "abc 123 def 456" ) );
		});
	}

	public void function testPosixUpper() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "ABC", "XYZ" ], reMatch( "[[:upper:]]+", "abc ABC xyz XYZ" ) );
		});
	}

	public void function testEscapeInsideClass() {
		variables._regex.eachEngine( function( engine ) {
			// match literal . and , and -
			assertEquals( [ ".", ",", "-" ], reMatch( "[\.\,\-]", "a.b,c-d" ) );
		});
	}

	public void function testEscapeBackslashInsideClass() {
		variables._regex.eachEngine( function( engine ) {
			// [\\] matches a literal backslash
			assertEquals( [ "\" ], reMatch( "[\\]", "a\b" ) );
		});
	}

	public void function testEscapeClosingBracketInsideClass() {
		variables._regex.eachEngine( function( engine ) {
			// [\]] matches a literal closing bracket
			assertEquals( [ "]" ], reMatch( "[\]]", "a]b" ) );
		});
	}

	public void function testPosixSpace() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			// [[:space:]] — worked example from Adobe docs: replaces spaces with *
			assertEquals( "Adobe*Web*Site", reReplace( "Adobe Web Site", "[[:space:]]", "*", "all" ) );
		});
	}

	public void function testPosixAlnum() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc123" ], reMatch( "[[:alnum:]]+", "abc123 !" ) );
		});
	}

	public void function testPosixLower() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc", "xyz" ], reMatch( "[[:lower:]]+", "abc ABC xyz" ) );
		});
	}

	// [[:blank:]] is narrower than [[:space:]] — includes spaces but NOT newlines
	public void function testPosixBlankMatchesSpace() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ " " ], reMatch( "[[:blank:]]+", "a b" ) );
		});
	}

	public void function testPosixBlankExcludesNewline() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, arrayLen( reMatch( "[[:blank:]]+", chr(10) ) ) );
		});
	}

	public void function testPosixPunct() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "!", "?", "." ], reMatch( "[[:punct:]]", "hi! how? fine." ) );
		});
	}

	public void function testPosixXdigit() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "deadBEEF" ], reMatch( "[[:xdigit:]]+", "zz deadBEEF zz" ) );
		});
	}

	public void function testPosixCntrl() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			// chr(0)..chr(31) + chr(127) — tab and LF qualify, letters don't
			assertEquals( [ chr(9), chr(10) ], reMatch( "[[:cntrl:]]", "a" & chr(9) & "b" & chr(10) & "c" ) );
		});
	}

	// [[:graph:]] = printable non-space; [[:print:]] = printable including space
	public void function testPosixGraph() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc", "123" ], reMatch( "[[:graph:]]+", "abc 123" ) );
		});
	}

	public void function testPosixPrint() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			// space included, so whole string is one run
			assertEquals( [ "abc 123" ], reMatch( "[[:print:]]+", "abc 123" ) );
		});
	}

	// Union of two POSIX classes inside one bracket expression
	public void function testPosixCompoundClass() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc123", "xyz" ], reMatch( "[[:alpha:][:digit:]]+", "abc123 !! xyz" ) );
		});
	}

	// POSIX class inside an outer negation
	public void function testPosixInsideNegation() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc", "123" ], reMatch( "[^[:space:]]+", "abc 123" ) );
		});
	}

	// POSIX combined with a literal range in the same bracket
	public void function testPosixWithRange() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			// [[:digit:]A-F] — hex characters; matches the whole "DEAD123" run
			assertEquals( [ "DEAD123" ], reMatch( "[[:digit:]A-F]+", "DEAD123 xy" ) );
		});
	}

	public void function testShorthandDigit() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "123", "456" ], reMatch( "\d+", "abc 123 def 456" ) );
		});
	}

	public void function testShorthandWord() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "hello", "world" ], reMatch( "\w+", "hello world" ) );
		});
	}

	public void function testShorthandWhitespace() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ " ", " " ], reMatch( "\s+", "hello world foo" ) );
		});
	}

	public void function testShorthandNonDigit() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc " ], reMatch( "\D+", "abc 123" ) );
		});
	}

	public void function testShorthandNonWord() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ " ", "!" ], reMatch( "\W+", "hello world!" ) );
		});
	}

	public void function testShorthandNonWhitespace() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "hello", "world" ], reMatch( "\S+", "hello world" ) );
		});
	}

	// Java/compat support \p{Alpha} for POSIX alpha — perl/Oro does not understand \p{...}
	public void function testJavaPosixAlphaShorthand() unsupportedRegexEngine="perl" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc", "ABC" ], reMatch( "\p{Alpha}+", "abc 123 ABC" ) );
		});
	}
}
