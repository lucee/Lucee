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

	// perl's \b uses its Unicode \w (isLetterOrDigit + _), java's is ASCII.
	// compat translates \b and \B to lookarounds.

	public void function testWordBoundaryNonAscii() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "\bcaf#chr(233)#\b", "caf#chr(233)# au lait" ) );
			assertEquals( 0, reFind( "\b#chr(233)#", "caf#chr(233)#" ) );
			assertEquals( 1, reFind( "caf\B", "caf#chr(233)#" ) );
			// a combining acute isn't a word char
			assertEquals( 1, reFind( "e\b", "e#chr(769)#" ) );
		});
	}

	public void function testWordBoundaryNonAsciiAtStartPosition() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "\b#chr(233)#", "caf#chr(233)#", 4 ) );
			assertEquals( 4, reFind( "\B#chr(233)#", "caf#chr(233)#", 4 ) );
		});
	}

	public void function testWordBoundaryNonAsciiJavaUnchanged() unsupportedRegexEngine="perl,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "\bcaf#chr(233)#\b", "caf#chr(233)# au lait" ) );
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
		// compat now follows perl: it compiles non-multiline patterns with DOTALL.
		variables._regex.eachEngine( function( engine ) {
			var r = reFind( "a.b", "a#chr(10)#b" );
			if ( engine != "java" ) assertEquals( 1, r );
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

	// perl only treats \n as a line terminator, java also \r, \r\n, U+0085, U+2028 and U+2029.
	// compat compiles with UNIX_LINES.

	public void function testDollarNotBeforeCRLF() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "a$", "a#chr(13)##chr(10)#" ) );
			assertEquals( 1, reFind( "a$", "a#chr(10)#" ) );
		});
	}

	public void function testMultilineCaptureKeepsCR() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "a#chr(13)#", "b" ], reMatch( "(?m)^[^#chr(10)#]*$", "a#chr(13)##chr(10)#b" ) );
			assertEquals( [ "a#chr(13)#", "b" ], reMatch( "^(.*)$", "a#chr(13)##chr(10)#b", true ) );
		});
	}

	public void function testLineEndingsJavaUnchanged() unsupportedRegexEngine="perl,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "a$", "a#chr(13)##chr(10)#" ) );
			assertEquals( [ "a", "b" ], reMatch( "^(.*)$", "a#chr(13)##chr(10)#b", true ) );
			assertEquals( 3, reFind( "(?m)^b", "a#chr(13)#b" ) );
		});
	}

	public void function testMultilineCaretNotAfterCR() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "(?m)^b", "a#chr(13)#b" ) );
		});
	}

	// A start position only moves where the search begins, the input still starts at 1:
	// ^ and \A don't match at the start position, \b and \B see the char before it.
	// Runs on all engines, java included, see LDEV-6798.

	public void function testCaretNotAtStartPosition() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "^b", "ab", 2 ) );
			assertEquals( 0, reFind( "\Ab", "ab", 2 ) );
			assertEquals( 1, reFind( "^a", "ab", 1 ) );
		});
	}

	public void function testWordBoundarySeesCharBeforeStartPosition() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "\bb", "ab", 2 ) );
			assertEquals( 2, reFind( "\Bb", "ab", 2 ) );
			assertEquals( 3, reFind( "\bb", "a b", 2 ) );
		});
	}

	public void function testMultilineCaretAtStartPosition() {
		variables._regex.eachEngine( function( engine ) {
			// Oro quirk: a multiline ^ never matches at the start position, even right after \n.
			// ACF Perl and java match there, compat doesn't emulate it (known divergence)
			var r = reFind( "(?m)^b", "a#chr(10)#b", 3 );
			if ( engine == "perl" ) assertEquals( 0, r );
			else                    assertEquals( 3, r );
			assertEquals( 0, reFind( "(?m)^b", "ab", 2 ) );
		});
	}

	public void function testCaretAtStartPositionScopeAll() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ 3 ], reFind( "(^|,)b", "ab,b", 2, false, "all" ) );
		});
	}

	public void function testCaretAtStartPositionPresideReSearch() {
		// Preside PresideObjectService._reSearch() restarts reFindNoCase one past each match
		variables._regex.eachEngine( function( engine ) {
			var r = reFindNoCase( "(^|\.)task", "taskmanager_task.task_key", 13, true );
			assertEquals( 17, r.pos[ 1 ] );
			assertEquals( ".task", r.match[ 1 ] );
		});
	}
}
