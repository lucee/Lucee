// Dot-vs-newline semantics. Lucee's perl/Oro engine treats `.` as matching
// every character including \n and \r (documented Lucee/ACF exception to
// standard Perl). Java/compat defaults to dot-NOT-newline; (?s) lifts the
// restriction. Pins the regression LDEV-2156 surfaced — compat should match
// perl behaviour once the pattern translator lands.
// compat: DOTALL + UNIX_LINES for non-multiline patterns.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	// === default dot-matches-newline behaviour (perl semantics) ===

	public void function testDotMatchesLineFeed() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, arrayLen( reMatch( ".", chr(10) ) ) );
		});
	}

	public void function testDotMatchesCarriageReturn() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, arrayLen( reMatch( ".", chr(13) ) ) );
		});
	}

	public void function testDotMatchesCRLF() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// two chars, dot matches each individually
			assertEquals( 2, arrayLen( reMatch( ".", chr(13) & chr(10) ) ) );
		});
	}

	public void function testDotConsumesNewlineInsideQuantifier() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// `a.+d` consumes the embedded \n in perl — whole input matches as one run
			var input = "ab" & chr(10) & "cd";
			assertEquals( [ input ], reMatch( "a.+d", input ) );
		});
	}

	public void function testDotMatchesNewlineInReFind() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, reFind( "a.b", "a" & chr(10) & "b" ) );
		});
	}

	public void function testDotMatchesNewlineInReReplace() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// replace `a.b` with `X` — newline gets eaten with surrounding chars
			assertEquals( "X", reReplace( "a" & chr(10) & "b", "a.b", "X" ) );
		});
	}

	public void function testDotStripsMultilineComment() unsupportedRegexEngine="java" {
		// Preside PresideObjectViewService.cfc
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "xy", reReplace( "x<!-- a" & chr(10) & "b -->y", "<!--.*?-->", "", "all" ) );
		});
	}

	public void function testDotNotNewlineWhenMultiline() {
		// multiline=true compiles without SINGLELINE_MASK / DOTALL on every engine
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [], reMatch( "a.b", "a" & chr(10) & "b", true ) );
		});
	}


	// === (?s) inline DOTALL — lifts restriction on every engine ===

	public void function testDotallFlagMatchesLineFeed() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 1, arrayLen( reMatch( "(?s).", chr(10) ) ) );
		});
	}

	public void function testDotallFlagConsumesAcrossLines() {
		variables._regex.eachEngine( function( engine ) {
			var input = "ab" & chr(10) & "cd";
			assertEquals( [ input ], reMatch( "(?s)a.+d", input ) );
		});
	}


	// === sanity baseline: dot matches every non-newline char regardless of engine ===

	public void function testDotMatchesSpace() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "a b" ], reMatch( "a.b", "a b" ) );
		});
	}

	public void function testDotMatchesPunct() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "a@b" ], reMatch( "a.b", "a@b" ) );
		});
	}

	public void function testDotMatchesDigit() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "a5b" ], reMatch( "a.b", "a5b" ) );
		});
	}


	// === big input, failing match: dot across lines vs backtracking (LDEV-2156) ===

	public void function testBigInputNoMatchMultiline() {
		// the LDEV-2156 workaround: multiline=true keeps every .{0,} on one line
		var input = bigNavHtml();
		variables._regex.eachEngine( function( engine ) {
			var start = getTickCount();
			assertEquals( [], reMatchNoCase( regex=ldev2156Pattern(), string=input, multiline=true ) );
			systemOutput( "LDEV-2156 multiline engine=#engine# #getTickCount() - start#ms", true );
		});
	}

	public void function testBigInputNoMatch() unsupportedRegexEngine="java" {
		// without multiline, .{0,} can run to the end of the input from every <a, and the match fails
		var input = bigNavHtml();
		variables._regex.eachEngine( function( engine ) {
			var start = getTickCount();
			assertEquals( [], reMatchNoCase( ldev2156Pattern(), input ) );
			systemOutput( "LDEV-2156 dot-newline engine=#engine# #getTickCount() - start#ms", true );
		});
	}

	private string function ldev2156Pattern() {
		var domain = "(http|https):\/\/(dev|test|staging|www)\.dev\.testingsite\.com";
		return "(<(a|img)[[:blank:]].{0,}(href|src).{0,})(=(" & '"' & "|')" & domain & "(\/(includes|files)\/))(.{0,})";
	}

	private string function bigNavHtml() {
		// nav menu with no link on the domain, same shape as the original report (test/tickets/LDEV2156/test.cfm)
		// the original 465 links took ~25s per engine without multiline, 60 links ~0.5s, cost grows roughly with links^2
		var lines = [ "<div>", "<nav><ul>" ];
		for ( var i = 1; i <= 60; i++ ) {
			arrayAppend( lines, '<li><a href="/listmaintain/#i * 7919#/item-#i#-hdghdsjfhgjdsgf.html">item #i#</a></li>' );
		}
		arrayAppend( lines, "</ul></nav></div>" );
		return arrayToList( lines, chr(10) );
	}


	// === character-class equivalents always work on every engine ===

	public void function testCharClassNewlineExplicit() {
		variables._regex.eachEngine( function( engine ) {
			// [\s\S] is the engine-portable "match any character including newline"
			var input = "ab" & chr(10) & "cd";
			assertEquals( [ input ], reMatch( "a[\s\S]+d", input ) );
		});
	}
}
