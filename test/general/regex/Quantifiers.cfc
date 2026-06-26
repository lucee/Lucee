// Quantifiers: * + ? {n,m} and lazy variants. Possessive *+ ++ is java-only.
// testStarGreedyZeroOrMore captures the LDEV-3703 trailing-empty-match divergence on java.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	public void function testStarGreedyZeroOrMore() {
		// LDEV-3703 family: java appends a trailing empty match at end of input
		variables._regex.eachEngine( function( engine ) {
			var r = reMatch( "a*", "aaabbb" );
			if ( engine == "perl" ) assertEquals( [ "aaa", "", "", "" ],     r );
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
			assertEquals( [ "aaa", "", "", "", "" ], reMatch( "a*+", "aaabbb" ) );
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
}
