// Anchors: ^ $ for line/string boundaries, (?m) for multiline mode,
// \A and \z for absolute start/end. Both engines support these.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	public void function testCaretMatchesStart() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "^hello", "hello world" ) );
		});
	}

	public void function testCaretFailsMidString() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "^world", "hello world" ) );
		});
	}

	public void function testDollarMatchesEnd() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 7, reFind( "world$", "hello world" ) );
		});
	}

	public void function testDollarFailsMidString() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "hello$", "hello world" ) );
		});
	}

	public void function testMultilineCaretFindsEachLineStart() {
		variables._regex.eachEngine( function( engine ) {
			// (?m) flag — ^ matches at every line start
			var matches = reMatch( "(?m)^\w+", "alpha#chr(10)#beta#chr(10)#gamma" );
			assertEquals( [ "alpha", "beta", "gamma" ], matches );
		});
	}

	public void function testWordBoundary() {
		variables._regex.eachEngine( function( engine ) {
			// \b word boundary — find "cat" but not "catalog"
			assertEquals( [ "cat" ], reMatch( "\bcat\b", "the cat and the catalog" ) );
		});
	}

	public void function testNotWordBoundary() {
		variables._regex.eachEngine( function( engine ) {
			// \B — NOT at a word boundary — "cat" inside a word matches, standalone doesn't
			assertEquals( [ "cat" ], reMatch( "\Bcat\B", "concatenate" ) );
			assertEquals( 0, arrayLen( reMatch( "\Bcat\B", "the cat sat" ) ) );
		});
	}

	public void function testAbsoluteStartAnchor() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "\Ahello", "hello world" ) );
			assertEquals( 0, reFind( "\Aworld", "hello world" ) );
		});
	}

	// Perl/Oro does not implement \z (lowercase) — it treats it as a literal z and returns 0.
	// Java and compat honour \z as an absolute end-of-string anchor.
	public void function testAbsoluteEndAnchor() unsupportedRegexEngine="perl" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 7, reFind( "world\z", "hello world" ) );
			assertEquals( 0, reFind( "hello\z", "hello world" ) );
		});
	}

	public void function testAbsoluteAnchorIgnoresMultiline() {
		variables._regex.eachEngine( function( engine ) {
			// \A is unaffected by (?m) — matches only the absolute start of the string
			assertEquals( [ "start" ], reMatch( "(?m)\Astart", "start#chr(10)#start" ) );
			// contrast: (?m)^ matches at the start of every line
			assertEquals( [ "start", "start" ], reMatch( "(?m)^start", "start#chr(10)#start" ) );
		});
	}

	public void function testDotDefaultNewlineBehavior() {
		// Perl/Oro: "A period always matches newlines" (documented Lucee/ACF exception to Perl standard).
		// Java/compat: dot does not match newlines without (?s).
		variables._regex.eachEngine( function( engine ) {
			var r = reFind( "a.b", "a#chr(10)#b" );
			if ( engine == "perl" ) assertEquals( 1, r );
			else                    assertEquals( 0, r );
		});
	}

	public void function testDotWithSFlagMatchesNewlineOnBothEngines() {
		variables._regex.eachEngine( function( engine ) {
			// (?s) makes dot match newlines on both engines
			assertEquals( 1, reFind( "(?s)a.b", "a#chr(10)#b" ) );
		});
	}

	public void function testInlineCaseInsensitiveModifier() {
		variables._regex.eachEngine( function( engine ) {
			// (?i) inline flag — case-insensitive for the whole expression
			assertEquals( 1, reFind( "(?i)hello", "HELLO world" ) );
			assertEquals( 0, reFind( "hello",     "HELLO world" ) );
		});
	}
}
