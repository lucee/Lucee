// Replacement-string escaping on the perl engine: Perl5Util.escape() rewrites the
// CFML replacement into ORO Perl5Substitution syntax before every replace. Plain
// literals must come out unchanged, and each special char (\ + ? $ ^) keeps its
// current meaning. Expected values were captured from the perl engine as-is.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }

	public void function testLiteralReplacement() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "a-c", reReplace( "aBc", "(B)", "-" ) );
			assertEquals( "a-b-c", reReplace( "a b c", " ", "-", "all" ) );
			assertEquals( "a-b-c", reReplaceNoCase( "aXbxc", "x", "-", "all" ) );
		});
	}

	public void function testEmptyReplacement() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "ac", reReplace( "aBc", "(B)", "" ) );
			assertEquals( "abc", reReplace( "a b c", " ", "", "all" ) );
		});
	}

	public void function testLiteralNonSpecialPunctuation() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "a&{}%*.|c", reReplace( "aBc", "(B)", "&{}%*.|" ) );
		});
	}

	public void function testPlus() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "a+c", reReplace( "aBc", "(B)", "+" ) );
		});
	}

	public void function testQuestion() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "a?c", reReplace( "aBc", "(B)", "?" ) );
		});
	}

	public void function testDollar() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "a$c", reReplace( "aBc", "(B)", "$" ) );
			assertEquals( "a$1c", reReplace( "aBc", "(B)", "$1" ) );
		});
	}

	public void function testCaret() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "a^c", reReplace( "aBc", "(B)", "^" ) );
		});
	}

	public void function testBackslash() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "aBc", reReplace( "aBc", "(B)", "\1" ) );
			assertEquals( "a\Bc", reReplace( "aBc", "(B)", "\\1" ) );
			assertEquals( "a\c", reReplace( "aBc", "(B)", "\" ) );
			assertEquals( "a\\c", reReplace( "aBc", "(B)", "\\" ) );
			assertEquals( "a\nc", reReplace( "aBc", "(B)", "\n" ) );
		});
	}

	public void function testCaseEscapes() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "aBc", reReplace( "aBc", "(B)", "\U\1" ) );
			assertEquals( "a\Bc", reReplace( "aBc", "(B)", "\\U\1" ) );
			assertEquals( "abc", reReplace( "aBc", "(B)", "\L\1" ) );
		});
	}

	public void function testMixed() unsupportedRegexEngine="java,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "a<B+?>c", reReplace( "aBc", "(B)", "<\1+?>" ) );
		});
	}
}
