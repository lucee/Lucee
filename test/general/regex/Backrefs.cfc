// Backref expansion in replacement string: \1..\9 numbered group refs.
// Currently a perl-engine feature; java raw uses $N. The LDEV-6432 translator
// will make \N work on java compat=true — at which point this file's
// unsupportedRegexEngine="java" stays (raw java still doesn't support \N)
// and the matrix-added "java-compat" entry picks it up automatically.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	public void function testBackrefReverseThreeGroups() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "cba", reReplace( "abc", "(a)(b)(c)", "\3\2\1" ) );
		});
	}

	public void function testBackrefSingleGroupScopeAll() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "[a][b][c]", reReplace( "abc", "(.)", "[\1]", "all" ) );
		});
	}

	public void function testBackrefNoMatchReturnsInputUnchanged() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "abc", reReplace( "abc", "x(.)y", "\1" ) );
		});
	}

	public void function testBackrefNestedGroups() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// outer = "ab", inner = "a"
			assertEquals( "ab-a", reReplace( "ab", "((.).)", "\1-\2" ) );
		});
	}

	public void function testBackrefRepeatedGroupReference() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "a-a-a", reReplace( "abc", "(.).*", "\1-\1-\1" ) );
		});
	}

	public void function testReplaceAllZeroMatchPreservesInput() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "abc", reReplace( "abc", "x", "y", "all" ) );
		});
	}

	// compat: \0 expands to group(0) — the full match, not a null byte
	public void function testBackrefGroup0FullMatchCompat() unsupportedRegexEngine="java,perl" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "x[abc]x", reReplace( "xabcx", "(a)(b)(c)", "[\0]" ) );
		});
	}

	// literal $ in replacement with no backslash — fast-path must not eat it
	public void function testLiteralDollarInReplacement() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "price $5", reReplace( "foo", "foo", "price $5" ) );
		});
	}

	// compat: dangling \ at end of replacement string is emitted as a literal backslash
	public void function testTrailingBackslashEmittedLiteralCompat() unsupportedRegexEngine="java,perl" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "x\", reReplace( "abc", "abc", "x\" ) );
		});
	}
}
