// Quantifiers: * + ? {n,m} and lazy variants. Possessive *+ ++ is java-only.
// testStarGreedyZeroOrMore captures the LDEV-3703 trailing-empty-match divergence on java.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	public void function testStarGreedyZeroOrMore() {
		// LDEV-3703 family: java appends a trailing empty match at end of input
		// compat follows perl: no empty match straight after a non-empty one
		variables._regex.eachEngine( function( engine ) {
			var r = reMatch( "a*", "aaabbb" );
			if ( engine != "java" ) assertEquals( [ "aaa", "", "", "" ],     r );
			else                    assertEquals( [ "aaa", "", "", "", "" ], r );
		});
	}

	public void function testPlusOneOrMore() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "aaa" ], reMatch( "a+", "aaabbb" ) );
		});
	}

	public void function testQuestionZeroOrOne() {
		variables._regex.eachEngine( function( engine ) {
			// match optional "s" suffix
			assertEquals( [ "cats", "dog" ], reMatch( "(?:cat|dog)s?", "cats and dog" ) );
		});
	}

	public void function testExactCount() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "aaa" ], reMatch( "a{3}", "aaaa" ) );
		});
	}

	public void function testMinMaxCount() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "aaaa", "aaa" ], reMatch( "a{2,4}", "aaaaa aaa" ) );
		});
	}

	public void function testMinOnly() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "aaaa" ], reMatch( "a{2,}", "aaaa" ) );
		});
	}

	public void function testLazyStar() {
		variables._regex.eachEngine( function( engine ) {
			// lazy match — shortest possible between <>
			assertEquals( [ "<a>", "<b>" ], reMatch( "<.*?>", "<a><b>" ) );
		});
	}

	public void function testGreedyStarContrast() {
		variables._regex.eachEngine( function( engine ) {
			// greedy match — longest possible
			assertEquals( [ "<a><b>" ], reMatch( "<.*>", "<a><b>" ) );
		});
	}

	public void function testPossessiveQuantifierJavaOnly() unsupportedRegexEngine="perl" {
		variables._regex.eachEngine( function( engine ) {
			// possessive a*+ — no backtracking. Empty matches still happen at each
			// non-a position; trailing empty at end of input is the java-engine norm.
			// compat follows perl: no empty match straight after "aaa"
			var r = reMatch( "a*+", "aaabbb" );
			if ( engine == "compat" ) assertEquals( [ "aaa", "", "", "" ],     r );
			else                      assertEquals( [ "aaa", "", "", "", "" ], r );
		});
	}

	public void function testLazyPlus() {
		variables._regex.eachEngine( function( engine ) {
			// +? — lazy one-or-more: matches shortest possible run of content
			assertEquals( [ "<a>", "<b>" ], reMatch( "<.+?>", "<a><b>" ) );
		});
	}

	public void function testLazyQuestion() {
		variables._regex.eachEngine( function( engine ) {
			// ?? — lazy zero-or-one: prefers to skip the optional char, backtracks if needed
			assertEquals( [ "color",  "colour" ], reMatch( "colou??r", "color and colour" ) );
		});
	}

	public void function testLazyRange() {
		variables._regex.eachEngine( function( engine ) {
			// {n,m}? — lazy range: matches fewest chars within bounds
			assertEquals( [ "<a>", "<bb>" ], reMatch( "<.{1,5}?>", "<a><bb>" ) );
		});
	}

	public void function testZeroWidthReplaceInsertsAtEveryPosition() {
		// [T]* matches zero T's at every position — inserts replacement between every character.
		// Adobe docs: REReplace("Hello","[T]*","7","ALL") → "7H7e7l7l7o7"
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "7H7e7l7l7o7", reReplace( "Hello", "[T]*", "7", "all" ) );
		});
	}

	// A { that doesn't start a valid quantifier is a literal on perl, java throws "Illegal repetition".
	// compat escapes it.

	public void function testLiteralDoubleBraces() unsupportedRegexEngine="java" {
		// Preside SqlRunner.cfc
		variables._regex.eachEngine( function( engine ) {
			var r = reFindNoCase( "{{base64:([A-Za-z0-9\+\/=]+)}}", "x {{base64:QUJD}} y", 1, true );
			assertEquals( 3, r.pos[ 1 ] );
			assertEquals( "QUJD", r.match[ 2 ] );
		});
	}

	public void function testLiteralBraceAfterClass() unsupportedRegexEngine="java" {
		// Wheels maintenance/on.cfc
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "function onRequestStart() {X", reReplace( "function onRequestStart() {", "(function\s+onRequestStart[^{]*{)", "\1X" ) );
		});
	}

	public void function testLiteralBraceAlternation() unsupportedRegexEngine="java" {
		// ColdBox RoutingService.cfc
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "id", reReplaceNoCase( "{id}", "({|})", "", "all" ) );
			assertEquals( [ "{id}", "{slug}" ], reMatchNoCase( "{[^{]+?}", "/a/{id}/{slug}" ) );
		});
	}

	public void function testLoneBraceIsLiteral() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "a{b" ], reMatch( "a{b", "a{b" ) );
			assertEquals( [ "a}b" ], reMatch( "a}b", "a}b" ) );
			assertEquals( [ "${x}" ], reMatch( "\${x}", "${x}" ) );
		});
	}

	public void function testBraceWithoutMinIsLiteral() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "a{,2}" ], reMatch( "a{,2}", "a{,2}" ) );
		});
	}

	public void function testLeadingBraceIsLiteral() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "{2}a" ], reMatch( "{2}a", "{2}a" ) );
		});
	}

	public void function testNonAsciiDigitBrace() {
		// Oro reads {U+0662} as a quantifier that never matches, compat treats it as a literal
		// (known divergence), java throws
		variables._regex.eachEngine( function( engine ) {
			var p = "a{#chr(1634)#}";
			if ( engine == "perl" ) assertEquals( [], reMatch( p, p ) );
			else if ( engine == "compat" ) assertEquals( [ p ], reMatch( p, p ) );
			else assertTrue( variables._regex.throws( function() { reMatch( p, p ); } ) );
		});
	}

	public void function testBracesJavaUnchanged() unsupportedRegexEngine="perl,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertTrue( variables._regex.throws( function() { reFind( "{{x}}", "{{x}}" ); } ) );
			assertTrue( variables._regex.throws( function() { reFind( "a{b", "a{b" ); } ) );
			// a leading {2} quantifies nothing on java
			assertEquals( [ "a" ], reMatch( "{2}a", "{2}a" ) );
		});
	}

	public void function testSyntaxErrorShowsRawPattern() unsupportedRegexEngine="perl,java" {
		// the error names the pattern as written, not compat's translation of it
		variables._regex.eachEngine( function( engine ) {
			try {
				reFind( "{(a", "x" );
				fail( "expected a syntax error" );
			} catch ( any e ) {
				assertTrue( find( "{(a", e.message ) > 0, e.message );
				assertEquals( 0, find( "\{", e.message ), e.message );
			}
		});
	}

	public void function testBraceQuantifiersStillQuantify() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "aa" ], reMatch( "a{2}", "aaa" ) );
			assertEquals( [ "aaa" ], reMatch( "a{2,}", "aaa" ) );
			assertEquals( [ "aaa" ], reMatch( "a{2,3}", "aaaa" ) );
			assertEquals( [ "{2}" ], reMatch( "\{2\}", "a{2}" ) );
		});
	}

	// perl doesn't report an empty match where the previous match ended, java does
	public void function testEmptyMatchAfterMatchSkipped() {
		variables._regex.eachEngine( function( engine ) {
			if ( engine != "java" ) {
				assertEquals( [ "ab", "c" ], reMatch( "[^\[]*", "ab[c" ) );
				assertEquals( [ "ab" ], reMatch( "(a|b)*", "ab" ) );
			} else {
				assertEquals( [ "ab", "", "c", "" ], reMatch( "[^\[]*", "ab[c" ) );
				assertEquals( [ "ab", "" ], reMatch( "(a|b)*", "ab" ) );
			}
		});
	}

	// perl retries at the same position for a non-empty match, java moves on a char.
	// compat skips the empty match but doesn't retry (known divergence, rare pattern shape)
	public void function testEmptyAlternativeRetriedAfterMatch() {
		variables._regex.eachEngine( function( engine ) {
			var r = reMatch( "a|b??", "ab" );
			if ( engine == "perl" )        assertEquals( [ "a", "b" ], r );
			else if ( engine == "compat" ) assertEquals( [ "a", "" ], r );
			else                           assertEquals( [ "a", "", "" ], r );
		});
	}

	public void function testReplaceAllSkipsEmptyMatchAfterMatch() {
		variables._regex.eachEngine( function( engine ) {
			var r = reReplace( "ab", "(a|b)*", "X", "all" );
			if ( engine != "java" ) assertEquals( "X", r );
			else                    assertEquals( "XX", r );
		});
	}

	// In (?x), Oro skips whitespace and comments inside a run of literals after it has checked for a quantifier,
	// so a quantifier after the whitespace is a literal. Perl and java read it as a quantifier.
	// compat escapes it.

	public void function testExtendedSpaceBeforeQuantifierIsLiteral() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "(?x)a *b", "a*b" ) );
			assertEquals( 0, reFind( "(?x)a *b", "aab" ) );
			assertEquals( 1, reFind( "(?x)a +b", "a+b" ) );
			assertEquals( 1, reFind( "(?x)a ?b", "a?b" ) );
			assertEquals( 1, reFind( "(?x)a {2}b", "a{2}b" ) );
			assertEquals( 0, reFind( "(?x)a {2}b", "aab" ) );
			assertEquals( 1, reFind( "(?x)ab * c", "ab*c" ) );
		});
	}

	public void function testExtendedWhitespaceKindsBeforeQuantifier() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "(?x)a  *b", "a*b" ) );
			assertEquals( 1, reFind( "(?x)a#chr(9)#*b", "a*b" ) );
			assertEquals( 1, reFind( "(?x)a#chr(10)#*b", "a*b" ) );
			assertEquals( 1, reFind( "(?x)a ##c#chr(10)#*b", "a*b" ) );
		});
	}

	public void function testExtendedEscapedLiteralInRun() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "(?x)a\. *b", "a.*b" ) );
			assertEquals( 0, reFind( "(?x)a\. *b", "a..b" ) );
		});
	}

	public void function testExtendedSecondQuantifierTakesWholeRun() unsupportedRegexEngine="java" {
		// the literal * is backed off by the ? after it, and the ? then quantifies the whole run: (?:ab)*?c
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "ababc" ], reMatch( "(?x)ab *?c", "ababc" ) );
			assertEquals( 4, reFind( "(?x)ab *?c", "ab*c" ) );
		});
	}

	public void function testExtendedQuantifierOutsideRun() {
		// after a group, a class or an escape like \d there's no run, so all engines quantify
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 3, reFind( "(?x)(a) *b", "a*b" ) );
			assertEquals( 3, reFind( "(?x)[a] *b", "a*b" ) );
			assertEquals( 1, reFind( "(?x)\d *b", "11b" ) );
			assertEquals( 1, reFind( "(?x)a* b", "aab" ) );
		});
	}

	// Oro's (?x) also differs on what it skips: whitespace in a class is literal, it stays on to the end
	// of the pattern rather than the group, and a unicode space is only skipped between atoms

	public void function testExtendedWhitespaceInClassIsLiteral() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 2, reFind( "(?x)[ ]", "a b" ) );
			assertEquals( [ "a b" ], reMatch( "(?x)[a b]+", "a b" ) );
		});
	}

	public void function testExtendedAppliesToEndOfPattern() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "((?x)a b) c", "abc" ) );
			assertEquals( 0, reFind( "((?x)a b) c", "ab c" ) );
			assertEquals( 1, reFind( "(?x)a b(?-x) c", "ab c" ) );
			assertEquals( 1, reFind( "(?ix)A b", "ab" ) );
		});
	}

	public void function testExtendedUnicodeSpace() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "(?x)a#chr(8195)#b", "a#chr(8195)#b" ) );
			assertEquals( 0, reFind( "(?x)a#chr(8195)#b", "ab" ) );
			assertEquals( 1, reFind( "(?x)(a)#chr(8195)#b", "ab" ) );
		});
	}

	public void function testExtendedJavaUnchanged() unsupportedRegexEngine="perl,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "(?x)a *b", "aab" ) );
			assertEquals( 3, reFind( "(?x)a *b", "a*b" ) );
		});
	}
}
