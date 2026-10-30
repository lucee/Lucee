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
	// compat translates `[:name:]` inside a class to `\p{Name}`.

	public void function testPosixAlpha() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc", "ABC" ], reMatch( "[[:alpha:]]+", "abc 123 ABC" ) );
		});
	}

	public void function testPosixDigit() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "123", "456" ], reMatch( "[[:digit:]]+", "abc 123 def 456" ) );
		});
	}

	public void function testPosixUpper() unsupportedRegexEngine="java" {
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

	public void function testPosixSpace() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// [[:space:]] — worked example from Adobe docs: replaces spaces with *
			assertEquals( "Adobe*Web*Site", reReplace( "Adobe Web Site", "[[:space:]]", "*", "all" ) );
		});
	}

	public void function testPosixAlnum() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc123" ], reMatch( "[[:alnum:]]+", "abc123 !" ) );
		});
	}

	public void function testPosixLower() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc", "xyz" ], reMatch( "[[:lower:]]+", "abc ABC xyz" ) );
		});
	}

	// [[:blank:]] is narrower than [[:space:]] — includes spaces but NOT newlines
	public void function testPosixBlankMatchesSpace() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ " " ], reMatch( "[[:blank:]]+", "a b" ) );
		});
	}

	public void function testPosixBlankExcludesNewline() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, arrayLen( reMatch( "[[:blank:]]+", chr(10) ) ) );
		});
	}

	public void function testPosixPunct() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "!", "?", "." ], reMatch( "[[:punct:]]", "hi! how? fine." ) );
		});
	}

	public void function testPosixXdigit() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "deadBEEF" ], reMatch( "[[:xdigit:]]+", "zz deadBEEF zz" ) );
		});
	}

	public void function testPosixCntrl() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// chr(0)..chr(31) + chr(127) — tab and LF qualify, letters don't
			assertEquals( [ chr(9), chr(10) ], reMatch( "[[:cntrl:]]", "a" & chr(9) & "b" & chr(10) & "c" ) );
		});
	}

	// [[:graph:]] = printable non-space; [[:print:]] = printable including space
	public void function testPosixGraph() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc", "123" ], reMatch( "[[:graph:]]+", "abc 123" ) );
		});
	}

	public void function testPosixPrint() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// space included, so whole string is one run
			assertEquals( [ "abc 123" ], reMatch( "[[:print:]]+", "abc 123" ) );
		});
	}

	// Union of two POSIX classes inside one bracket expression
	public void function testPosixCompoundClass() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc123", "xyz" ], reMatch( "[[:alpha:][:digit:]]+", "abc123 !! xyz" ) );
		});
	}

	// POSIX class inside an outer negation
	public void function testPosixInsideNegation() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc", "123" ], reMatch( "[^[:space:]]+", "abc 123" ) );
		});
	}

	// POSIX combined with a literal range in the same bracket
	public void function testPosixWithRange() unsupportedRegexEngine="java" {
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

	// perl's \w \d \s are Unicode (isLetterOrDigit + _, isDigit, isWhitespace), java's are ASCII.
	// compat translates them, UNICODE_CHARACTER_CLASS doesn't match perl (combining marks, nbsp).

	public void function testShorthandWordNonAscii() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "caf#chr(233)#", "au" ], reMatch( "\w+", "caf#chr(233)# au" ) );
			assertEquals( [ " " ], reMatch( "\W+", "caf#chr(233)# au" ) );
			// a combining acute isn't a letter or digit
			assertEquals( [ "e" ], reMatch( "\w+", "e#chr(769)#" ) );
		});
	}

	public void function testShorthandDigitNonAscii() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// Arabic-Indic three
			assertEquals( 1, reFind( "\d", chr(1635) ) );
			assertEquals( [ "a" ], reMatch( "\D+", "a#chr(1635)#" ) );
		});
	}

	public void function testShorthandWhitespaceNonAscii() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// em space and the file separator are whitespace, nbsp isn't
			assertEquals( 2, reFind( "\s", "a#chr(8195)#" ) );
			assertEquals( 2, reFind( "\s", "a#chr(28)#" ) );
			assertEquals( 0, reFind( "\s", "a#chr(160)#" ) );
			assertEquals( [ "a#chr(160)#b" ], reMatch( "\S+", "a#chr(160)#b#chr(8195)#" ) );
		});
	}

	public void function testShorthandInsideClassNonAscii() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "caf#chr(233)#-", "au" ], reMatch( "[\w-]+", "caf#chr(233)#- au" ) );
			assertEquals( [ " ", "!" ], reMatch( "[\W]+", "caf#chr(233)# au!" ) );
			assertEquals( [ "caf#chr(233)#" ], reMatch( "[^\W]+", "caf#chr(233)#!" ) );
			assertEquals( [ "1#chr(1635)#" ], reMatch( "[\d]+", "a1#chr(1635)#" ) );
			assertEquals( [ " #chr(8195)#" ], reMatch( "[\s]+", "a #chr(8195)#b" ) );
		});
	}

	// a - after \w or [:word:] is a literal, the translation mustn't end in a char that starts a range
	public void function testDashAfterShorthandWordInsideClass() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// Preside, via ColdBox
			assertEquals( 1, reFind( "[\w][\w-\.]*\:[\w][\w-\.]*[^\.]", "coldbox:my-app.x" ) );
			assertEquals( [ "a-.b_" ], reMatch( "[\w-\.]+", "a-.b_+" ) );
			assertEquals( [ "a-.b_" ], reMatch( "[[:word:]-.]+", "a-.b_+" ) );
			assertEquals( [ "a-", "b_" ], reMatch( "[\w-z]+", "a-.b_+" ) );
		});
	}

	public void function testShorthandNonAsciiJavaUnchanged() unsupportedRegexEngine="perl,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "caf", "au" ], reMatch( "\w+", "caf#chr(233)# au" ) );
			assertEquals( 0, reFind( "\d", chr(1635) ) );
			assertEquals( 0, reFind( "\s", "a#chr(8195)#" ) );
		});
	}

	// Java/compat support \p{Alpha} for POSIX alpha — perl/Oro does not understand \p{...}
	public void function testJavaPosixAlphaShorthand() unsupportedRegexEngine="perl" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "abc", "ABC" ], reMatch( "\p{Alpha}+", "abc 123 ABC" ) );
		});
	}

	// Wheels humanize(), Global.cfc
	public void function testPosixWheelsHumanize() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			var rv = reReplace( "firstNameURL", "([[:upper:]])", " \1", "all" );
			rv = reReplace( rv, "([[:punct:]])([[:space:]])", "\1", "all" );
			rv = reReplace( rv, "([[:upper:]]) ([[:upper:]])(?:\s|\b)", "\1\2", "all" );
			rv = reReplace( rv, "([[:upper:]])([[:upper:]])([[:lower:]])", "\1\2 \3", "all" );
			assertEquals( "first Name URL", rv );
		});
	}

	public void function testPosixLowerNoCase() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "ABC" ], reMatchNoCase( "[[:lower:]]+", "ABC 123" ) );
		});
	}

	public void function testPosixNegated() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "12-" ], reMatch( "[[:^alpha:]]+", "ab12-" ) );
			assertEquals( [ "ab" ], reMatch( "[^[:^alpha:]]+", "ab12-" ) );
			assertEquals( [ "-" ], reMatch( "[[:^word:]]+", "a_1-" ) );
			assertEquals( [ "1" ], reMatchNoCase( "[[:^lower:]]+", "abC1" ) );
		});
	}

	// An unescaped [ inside a class is a literal on perl, java reads it as a nested class.
	// compat escapes it.

	public void function testOpenBracketInsideClass() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "[" ], reMatch( "[[]", "a[b" ) );
			assertEquals( [ "ab", "c" ], reMatch( "[^[]+", "ab[c" ) );
			assertEquals( [ "ab[c" ], reMatch( "[a-z[]+", "ab[c" ) );
		});
	}

	public void function testOpenBracketInsideClassMasaBBML() unsupportedRegexEngine="java" {
		// Masa ParseBBML.cfm
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "<strong>x</strong>", reReplaceNoCase( "[b]x[/b]", "\[(b|bold)\]([^[]*)\[/\1\]", "<strong>\2</strong>" ) );
		});
	}

	public void function testBracketsInsideClassWheels() unsupportedRegexEngine="java" {
		// Wheels Test.cfc
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "abc", reReplace( "a(b)[c]", "[([\])]", "", "all" ) );
		});
	}

	public void function testAmpersandsInsideClassAreLiteral() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "a", "&", "&", "b" ], reMatch( "[a&&b]", "a&&b" ) );
		});
	}

	public void function testBackspaceInsideClass() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "ab", reReplace( "a" & chr(8) & "b", "[\b]", "", "all" ) );
		});
	}

	// Escapes java rejects that perl accepts

	// chr(0) returns "" on both Lucee and ACF, so it can't put a NUL into a string.
	// urlDecode( "%00" ) is the shortest way to get a real one.
	private string function nul() {
		return urlDecode( "%00" );
	}

	public void function testSingleDigitHexEscape() unsupportedRegexEngine="java" {
		// Masa settingsBundle.cfc
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "ab", reReplace( "a" & nul() & "b", "[\x0]", "", "all" ) );
		});
	}

	public void function testNulEscape() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "ab", reReplace( "a" & nul() & "b", "\0", "", "all" ) );
		});
	}

	public void function testHexEscapeWithoutDigits() unsupportedRegexEngine="java" {
		// Oro reads \x with no hex digits as NUL
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "axg", reReplace( "a" & nul() & "gxg", "\xg", "", "all" ) );
		});
	}

	public void function testUnknownLetterEscapeIsLiteral() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "q" ], reMatch( "\q", "aqb" ) );
			// java defines \k< and \N{, on their own they're literal letters on perl
			assertEquals( [ "k" ], reMatch( "\k", "aks" ) );
			assertEquals( [ "N" ], reMatch( "\N", "aNs" ) );
			// \E without a \Q
			assertEquals( [ "E" ], reMatch( "\E", "aEs" ) );
		});
	}

	public void function testLetterEscapeInsideClassIsLiteral() unsupportedRegexEngine="java" {
		// anchors and the other escapes java only allows outside a class
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "A", "B", "G", "Z", "z", "R", "X", "E" ], reMatch( "[\A\B\G\Z\z\R\X\E]", "aAbBgGZzRXEe" ) );
		});
	}

	public void function testControlEscapeLowerCase() unsupportedRegexEngine="java" {
		// perl upper cases the letter, \cz is \cZ
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 2, reFind( "\cz", "a#chr(26)#" ) );
			assertEquals( 2, reFind( "[\cz]", "a#chr(26)#" ) );
		});
	}

	public void function testOctalEscape() unsupportedRegexEngine="java" {
		// perl reads up to three octal digits, and \NN is octal unless that many groups came before it
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "\0123", "#chr(10)#3" ) );
			assertEquals( 1, reFind( "\101", "A" ) );
			assertEquals( 1, reFind( "(a)\18", "a#chr(1)#8" ) );
			assertEquals( 1, reFind( "(a)(b)(c)(d)(e)(f)(g)(h)(i)(j)\10", "abcdefghijj" ) );
			// inside a class digits are always octal
			assertEquals( [ "A" ], reMatch( "[\101]", "aAb" ) );
			assertEquals( 2, reFind( "[\12]", "a#chr(10)#" ) );
		});
	}

	public void function testClassesJavaUnchanged() unsupportedRegexEngine="perl,compat" {
		variables._regex.eachEngine( function( engine ) {
			// [[:upper:]] is a nested class of the chars : u p e r
			assertEquals( [ "e" ], reMatch( "[[:upper:]]+", "Hello WORLD" ) );
			assertTrue( variables._regex.throws( function() { reMatch( "[^[]+", "ab[c" ); } ) );
			assertTrue( variables._regex.throws( function() { reMatch( "[\b]", "a" ); } ) );
		});
	}

	public void function testEscapesJavaUnchanged() unsupportedRegexEngine="perl,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertTrue( variables._regex.throws( function() { reMatch( "[\x0]", "a" ); } ) );
			assertTrue( variables._regex.throws( function() { reMatch( "\xg", "a" ); } ) );
			assertTrue( variables._regex.throws( function() { reMatch( "\0", "a" ); } ) );
			assertTrue( variables._regex.throws( function() { reMatch( "\q", "a" ); } ) );
			assertTrue( variables._regex.throws( function() { reMatch( "\E", "a" ); } ) );
			assertTrue( variables._regex.throws( function() { reMatch( "[\B]", "a" ); } ) );
			assertTrue( variables._regex.throws( function() { reMatch( "[\101]", "a" ); } ) );
			assertEquals( 0, reFind( "\cz", chr(26) ) );
			assertEquals( 0, reFind( "\101", "A" ) );
		});
	}

	// Case-insensitive matching folds non-ASCII letters on perl, java only folds ASCII by default

	public void function testNoCaseNonAscii() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFindNoCase( chr(201), chr(233) ) );
			assertEquals( 1, reFindNoCase( "[#chr(192)#-#chr(221)#]", chr(233) ) );
			assertEquals( 1, reFindNoCase( chr(931), chr(963) ) );
		});
	}

	public void function testNoCaseNonAsciiJavaUnchanged() unsupportedRegexEngine="perl,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFindNoCase( chr(201), chr(233) ) );
		});
	}
}
