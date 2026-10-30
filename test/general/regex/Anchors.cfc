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

	// compat only tests one side of \b when a literal word char is on the other.
	// A group, a quantified literal and (?x) take the full lookaround, a single char between two \b takes both shortcuts
	public void function testWordBoundaryNonAsciiNotNextToLiteral() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 7, reFind( "\b(caf#chr(233)#)\b", "xcaf#chr(233)# caf#chr(233)#" ) );
			assertEquals( 0, reFind( "\b#chr(233)#+", "caf#chr(233)#" ) );
			assertEquals( 3, reFind( "\b#chr(233)#+", "a #chr(233)#" ) );
			// in (?x) perl reads a quantifier after whitespace in a literal run as a literal, see Quantifiers.cfc
			assertEquals( 0, reFind( "(?x)\b #chr(233)# * caf", "caf" ) );
			assertEquals( 1, reFind( "(?x)\b #chr(233)# * caf", "#chr(233)#*caf" ) );
			assertEquals( 3, reFind( "\b#chr(233)#\b", "a #chr(233)# b" ) );
			assertEquals( 0, reFind( "\b#chr(233)#\b", "caf#chr(233)#" ) );
			assertEquals( 2, reFind( "\B#chr(233)#\B", "f#chr(233)#e" ) );
		});
	}

	// the digits of a hex or octal escape aren't word chars, whatever they spell
	public void function testWordBoundaryAfterEscape() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "\x2e\b", ".a . " ) );
			assertEquals( 0, reFind( "\x2e\b", ". " ) );
			assertEquals( 1, reFind( "\056\b", ".a . " ) );
			assertEquals( 2, reFind( "\012\b", " #chr(10)#a" ) );
			assertEquals( 1, reFind( "\x41\b", "A AB" ) );
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

	// Oro compiles in two passes and the second starts with the modifiers the first ended with. So the last
	// (?m) (?s) (?x) also applies before it, and from where each one appears it applies to the end of the
	// pattern, not the end of the group like java's. The last (?i) applies to the whole pattern

	public void function testModifierAppliesBeforeIt() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "A(?i)bc", "abc" ) );
			assertEquals( 0, reFind( "a.c(?-s)x", "a#chr(10)#cx" ) );
			assertEquals( 1, reFind( "a$#chr(10)#b(?m)x", "a#chr(10)#bx" ) );
			assertEquals( 1, reFind( "a b(?x)c", "abc" ) );
		});
	}

	public void function testModifierAppliesToEndOfPattern() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "(a(?i)b)c", "aBC" ) );
			assertEquals( 1, reFind( "(?:(?m)a$)#chr(10)#^b", "a#chr(10)#b" ) );
			assertEquals( 0, reFind( "(a(?-s)b).c", "ab#chr(10)#c" ) );
			assertEquals( 1, reFind( "(a(?-s)b).c", "abxc" ) );
			assertEquals( 0, reFind( "(?-s)a.c(?s)x", "a#chr(10)#cx" ) );
			assertEquals( 1, reFind( "(?s)a.c(?-s)x", "a#chr(10)#cx" ) );
			assertEquals( 1, reFind( "(?m)a$#chr(10)#b(?-m)x", "a#chr(10)#bx" ) );
		});
	}

	public void function testModifierAfterGroupQuantifier() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "(?s)x.(a(?-s)b)+.c", "x#chr(10)#ab#chr(10)#c" ) );
			assertEquals( 1, reFind( "(?s)x.(a(?-s)b)+.c", "x#chr(10)#ababxc" ) );
			assertEquals( 0, reFind( "(?s)x.(a(?-s)b){2}.c", "x#chr(10)#abab#chr(10)#c" ) );
			assertEquals( 1, reFind( "(?s)x.(a(?-s)b){2}.c", "x#chr(10)#ababxc" ) );
			assertEquals( 0, reFind( "(?s)x.(a(?-s)b)*?.c", "x#chr(10)#ab#chr(10)#c" ) );
			assertEquals( 1, reFind( "(?s)x.(a(?-s)b)*?.c", "x#chr(10)#abxc" ) );
		});
	}

	public void function testModifierNestedGroupsAndAlternation() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "(?s)x.((a(?-s)b)c).d", "x#chr(10)#abc#chr(10)#d" ) );
			assertEquals( 1, reFind( "(?s)x.((a(?-s)b)c).d", "x#chr(10)#abcxd" ) );
			assertEquals( 0, reFind( "(?s)(a(?-s)b).|c.", "c#chr(10)#" ) );
			assertEquals( 1, reFind( "(?s)(a(?-s)b).|c.", "cx" ) );
		});
	}

	public void function testModifierCaseInsensitiveWholePattern() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "(?i)a(?-i)b", "Ab" ) );
			assertEquals( 1, reFind( "(?i)a(?-i)b", "ab" ) );
			assertEquals( 0, reFind( "(?i)ab(?-i)c", "ABc" ) );
			assertEquals( 0, reFind( "a(?i)(?-i)b", "AB" ) );
		});
	}

	public void function testModifierCaseInsensitiveUppercaseLiteral() {
		// Oro lowercases a literal while (?i) is on, and the input only if it's on at the end, so (?i)A matches
		// a but not A when a (?-i) follows. compat makes the whole pattern case sensitive, so it matches A but
		// not a (known divergence)
		variables._regex.eachEngine( function( engine ) {
			assertEquals( engine == "compat" ? 0 : 1, reFind( "(?i)A(?-i)bc", "abc" ) );
			assertEquals( engine == "perl" ? 0 : 1, reFind( "(?i)A(?-i)bc", "Abc" ) );
		});
	}

	// Oro accepts (?g) and (?o) and ignores them
	public void function testModifierGlobalAndOnce() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "(?g)a", "a" ) );
			assertEquals( 1, reFind( "(?o)a", "a" ) );
			assertEquals( 1, reFind( "(?gio)A", "a" ) );
		});
	}

	public void function testModifierJavaUnchanged() unsupportedRegexEngine="perl,compat" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "A(?i)bc", "abc" ) );
			assertEquals( 0, reFind( "(a(?i)b)c", "aBC" ) );
			assertEquals( 1, reFind( "(?i)(a(?-i)b)c", "AbC" ) );
			assertEquals( 1, reFind( "a b(?x)c", "a bc" ) );
			assertTrue( variables._regex.throws( function() { reFind( "(?g)a", "a" ); } ) );
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
	// java anchors at the start position (as ACF's java engine does), whether it should follow perl is LDEV-6798.

	public void function testCaretNotAtStartPosition() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "^b", "ab", 2 ) );
			assertEquals( 0, reFind( "\Ab", "ab", 2 ) );
			assertEquals( 1, reFind( "^a", "ab", 1 ) );
		});
	}

	public void function testWordBoundarySeesCharBeforeStartPosition() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "\bb", "ab", 2 ) );
			assertEquals( 2, reFind( "\Bb", "ab", 2 ) );
			assertEquals( 3, reFind( "\bb", "a b", 2 ) );
		});
	}

	public void function testMultilineCaretAtStartPosition() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// Oro quirk: a multiline ^ never matches at the start position, even right after \n.
			// ACF Perl matches there, compat doesn't emulate it (known divergence)
			var r = reFind( "(?m)^b", "a#chr(10)#b", 3 );
			if ( engine == "perl" ) assertEquals( 0, r );
			else                    assertEquals( 3, r );
			assertEquals( 0, reFind( "(?m)^b", "ab", 2 ) );
		});
	}

	public void function testCaretAtStartPositionScopeAll() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ 3 ], reFind( "(^|,)b", "ab,b", 2, false, "all" ) );
		});
	}

	public void function testCaretAtStartPositionPresideReSearch() unsupportedRegexEngine="java" {
		// Preside PresideObjectService._reSearch() restarts reFindNoCase one past each match
		variables._regex.eachEngine( function( engine ) {
			var r = reFindNoCase( "(^|\.)task", "taskmanager_task.task_key", 13, true );
			assertEquals( 17, r.pos[ 1 ] );
			assertEquals( ".task", r.match[ 1 ] );
		});
	}

	public void function testStartPositionJavaUnchanged() unsupportedRegexEngine="perl,compat" {
		// the input is cut at the start position: ^ and \A match there, \b sees no char before it
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 2, reFind( "^b", "ab", 2 ) );
			assertEquals( 2, reFind( "\Ab", "ab", 2 ) );
			assertEquals( 2, reFind( "(?m)^b", "ab", 2 ) );
			assertEquals( 2, reFind( "\bb", "ab", 2 ) );
			assertEquals( 0, reFind( "\Bb", "ab", 2 ) );
			assertEquals( [ 2, 3 ], reFind( "(^|,)b", "ab,b", 2, false, "all" ) );
			var r = reFindNoCase( "(^|\.)task", "taskmanager_task.task_key", 13, true );
			assertEquals( 13, r.pos[ 1 ] );
			assertEquals( "task", r.match[ 1 ] );
		});
	}
}
