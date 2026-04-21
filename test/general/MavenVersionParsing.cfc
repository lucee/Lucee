/**
 * Maven version parsing tests, using maven-resolver as the oracle.
 *
 * Many of the data cases below are ported from Apache Maven Resolver's own
 * unit tests (maven-resolver-util 2.0.16, Apache License 2.0):
 *
 *   maven-resolver-util/src/test/java/org/eclipse/aether/util/version/
 *     GenericVersionTest.java
 *     GenericVersionRangeTest.java
 *
 * These cases represent the canonical expected behaviour of maven version
 * comparison and range containment. We reuse them here as a ground-truth
 * corpus so that when Lucee gains its own MavenVersion implementation, it
 * can be validated against the exact same inputs that aether validates
 * against. Huge thanks to the Maven Resolver team for the rigorous test
 * coverage.
 *
 * Source: https://github.com/apache/maven-resolver/tree/maven-resolver-2.0.16
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven"
	javasettings='{"maven":["org.apache.maven.resolver:maven-resolver-util:2.0.16"]}' {

	function beforeAll() {
		variables.scheme = new org.eclipse.aether.util.version.GenericVersionScheme();
	}

	function run( testResults, testBox ) {

		describe( "maven-resolver (oracle): Version.compareTo — does maven-resolver's reference implementation return the expected sign?", function() {
			for ( var c in comparisonCases() ) {
				( function( c ) {
					it( title = "compare( '#c[ 1 ]#', '#c[ 2 ]#' ) -> sign #c[ 3 ]#", body = function( currentSpec ) {
						var left  = scheme.parseVersion( c[ 1 ] );
						var right = scheme.parseVersion( c[ 2 ] );
						var raw   = left.compareTo( right );
						var sign  = raw > 0 ? 1 : ( raw < 0 ? -1 : 0 );
						expect( sign ).toBe( c[ 3 ], "failed on #c.toJson()# (raw compareTo=#raw#)" );
					} );
				} )( c );
			}
		} );

		describe( "maven-resolver (oracle): VersionRange.containsVersion — does maven-resolver's reference implementation agree on range membership?", function() {
			for ( var c in rangeCases() ) {
				( function( c ) {
					it( title = "'#c[ 1 ]#' containsVersion '#c[ 2 ]#' -> #c[ 3 ]#", body = function( currentSpec ) {
						var constraint = scheme.parseVersionConstraint( c[ 1 ] );
						var version    = scheme.parseVersion( c[ 2 ] );
						expect( constraint.containsVersion( version ) ).toBe( c[ 3 ], "failed on #c.toJson()#" );
					} );
				} )( c );
			}
		} );

		// ---------------------------------------------------------------------
		// Baseline: Lucee's current MavenUtil.resolveVersionRange — the existing
		// "crappy" single-line substring hack. Locks in current behaviour so we
		// can see exactly what breaks when the real parser lands. Expected
		// values here are the CURRENT (often wrong) output.
		// ---------------------------------------------------------------------
		describe( "Lucee MavenUtil.resolveVersionRange (wired to MavenVersionRange, best-effort without maven-metadata.xml)", function() {
			for ( var c in currentResolveCases() ) {
				( function( c ) {
					it( title = "resolveVersionRange( '#c.input#' ) -> #c.current# [#c.verdict#]", body = function( currentSpec ) {
						var util = createObject( "java", "lucee.runtime.mvn.MavenUtil" );
						expect( util.resolveVersionRange( c.input ) ).toBe( c.current, "failed on #c.toJson()#" );
					} );
				} )( c );
			}
		} );

		// ---------------------------------------------------------------------
		// Lucee OSGiUtil.compare — Lucee already has an OSGi-flavoured version
		// comparator. Run maven's expected sign against it to expose exactly
		// which qualifier/ordering rules differ from maven semantics. Many of
		// these are expected to FAIL today; that's the point.
		// ---------------------------------------------------------------------
		xdescribe( "Lucee OSGiUtil.compare (OSGi semantics, not maven) — skipped: one-time proof that OSGi's comparator is NOT a drop-in for maven semantics. Kept for reference; unskip to re-run the divergence map.", function() {
			variables.osgiUtil = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
			variables.osgiVersion = createObject( "java", "org.osgi.framework.Version" );

			for ( var c in comparisonCases() ) {
				( function( c ) {
					it( title = "OSGiUtil.compare( '#c[ 1 ]#', '#c[ 2 ]#' ) -> sign #c[ 3 ]#", body = function( currentSpec ) {
						// org.osgi.framework.Version can't parse arbitrary maven strings; constructor
						// throws on e.g. "1.0-alpha-1" because it expects major.minor.micro.qualifier.
						var raw = "";
						try {
							var left  = osgiVersion.init( c[ 1 ] );
							var right = osgiVersion.init( c[ 2 ] );
							raw = osgiUtil.compare( left, right );
						}
						catch ( any e ) {
							fail( "OSGi Version rejected input #c.toJson()#: #e.message#" );
						}
						var sign = raw > 0 ? 1 : ( raw < 0 ? -1 : 0 );
						expect( sign ).toBe( c[ 3 ], "OSGi disagrees with maven on #c.toJson()# (OSGi sign=#sign#, maven expected=#c[ 3 ]#)" );
					} );
				} )( c );
			}
		} );

		// ---------------------------------------------------------------------
		// Lucee has NO maven-style VersionRange.containsVersion at all. The
		// closest thing — resolveVersionRange — returns a single string, so we
		// degenerate containsVersion to "does the resolved string equal the
		// tested version". Expect this to fail on most non-trivial cases;
		// that's the point.
		// ---------------------------------------------------------------------
		// ---------------------------------------------------------------------
		// Lucee's hand-rolled MavenVersion — the new implementation. Runs the
		// SAME 123 comparison cases through our parser + comparator. Failures
		// here are real bugs in our implementation.
		// ---------------------------------------------------------------------
		describe( "Lucee MavenVersion.compareTo (new hand-rolled impl) — does Lucee's new parser match maven?", function() {
			for ( var c in comparisonCases() ) {
				( function( c ) {
					it( title = "MavenVersion.compareTo( '#c[ 1 ]#', '#c[ 2 ]#' ) -> sign #c[ 3 ]#", body = function( currentSpec ) {
						var left  = createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( c[ 1 ] );
						var right = createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( c[ 2 ] );
						var raw   = left.compareTo( right );
						var sign  = raw > 0 ? 1 : ( raw < 0 ? -1 : 0 );
						expect( sign ).toBe( c[ 3 ], "Lucee MavenVersion disagrees with maven on #c.toJson()# (Lucee sign=#sign#, maven expected=#c[ 3 ]#)" );
					} );
				} )( c );
			}
		} );

		// ---------------------------------------------------------------------
		// Lucee's hand-rolled MavenVersionRange — the new implementation. Runs
		// the SAME 36 range cases through our parser + contains.
		// ---------------------------------------------------------------------
		describe( "Lucee MavenVersionRange.contains (new hand-rolled impl) — does Lucee's new range parser match maven?", function() {
			for ( var c in rangeCases() ) {
				( function( c ) {
					it( title = "MavenVersionRange( '#c[ 1 ]#' ).contains( '#c[ 2 ]#' ) -> #c[ 3 ]#", body = function( currentSpec ) {
						try {
							var range = createObject( "java", "lucee.runtime.mvn.MavenVersionRange" ).init( c[ 1 ] );
							expect( range.contains( c[ 2 ] ) ).toBe( c[ 3 ], "Lucee MavenVersionRange disagrees with maven on #c.toJson()#" );
						}
						catch ( any e ) {
							fail( "Lucee MavenVersionRange threw on #c.toJson()#: #e.message#" );
						}
					} );
				} )( c );
			}
		} );

		// ---------------------------------------------------------------------
		// Integrated corpus — drive pickHighest against realistic candidate
		// lists. Closes the gap between parse-level assertions (contains) and
		// the metadata-driven resolution path. Covers every range form plus
		// the pre-release filter. Expected picks reflect what the 5-arg
		// resolveVersionRange produces once metadata fetch returns the
		// candidate list.
		// ---------------------------------------------------------------------
		describe( "Lucee MavenVersionRange.pickHighest — integrated range + candidate-list resolution", function() {
			for ( var c in pickCases() ) {
				( function( c ) {
					var desc = "pickHighest( '#c.spec#', #c.candidates.toJson()# ) -> " & ( structKeyExists( c, "expected" ) ? "'#c.expected#'" : "null" );
					it( title = desc, body = function( currentSpec ) {
						var range = createObject( "java", "lucee.runtime.mvn.MavenVersionRange" ).init( c.spec );
						var list  = createObject( "java", "java.util.ArrayList" ).init();
						for ( var v in c.candidates ) list.add( v );
						local.picked = range.pickHighest( list );
						if ( structKeyExists( c, "expected" ) ) {
							expect( isNull( local.picked ) ).toBeFalse( "expected pick '#c.expected#' but got null for #c.toJson()#" );
							expect( local.picked.asString() ).toBe( c.expected, "failed on #c.toJson()#" );
						}
						else {
							expect( isNull( local.picked ) ).toBeTrue( "expected null pick but got non-null for #c.toJson()#" );
						}
					} );
				} )( c );
			}
		} );

		xdescribe( "Lucee degenerate containsVersion shim — skipped: one-time proof that resolveVersionRange alone cannot answer containment. Kept for reference; unskip to re-run.", function() {
			for ( var c in rangeCases() ) {
				( function( c ) {
					it( title = "'#c[ 1 ]#' containsVersion '#c[ 2 ]#' -> #c[ 3 ]#", body = function( currentSpec ) {
						var util = createObject( "java", "lucee.runtime.mvn.MavenUtil" );
						var shimResult = false;
						try {
							shimResult = ( util.resolveVersionRange( c[ 1 ] ) == c[ 2 ] );
						}
						catch ( any e ) {
							// resolveVersionRange threw — treat as "not contained" for the shim
							shimResult = false;
						}
						expect( shimResult ).toBe( c[ 3 ], "Lucee disagrees with maven on #c.toJson()# (shim=#shimResult#, maven expected=#c[ 3 ]#)" );
					} );
				} )( c );
			}
		} );
	}

	// ---------------------------------------------------------------------
	// Test data — kept in private helpers so additional describe blocks
	// (e.g. for Lucee's own hand-rolled implementation) can reuse them.
	// ---------------------------------------------------------------------

	/**
	 * Version comparison cases.
	 * Row: [ left, right, expected sign of left.compareTo(right) ]
	 *   -1 : left < right
	 *    0 : left == right
	 *    1 : left > right
	 */
	private array function comparisonCases() {
		return [
			// -------------------------------------------------------------
			// Lucee-local smoke cases
			// -------------------------------------------------------------
			[ "1.0",         "1.0",           0 ],
			[ "1.0",         "1.0.0",         0 ],
			[ "1.0",         "1.1",          -1 ],
			[ "1.1",         "1.0",           1 ],
			[ "1.2.3",       "1.2.4",        -1 ],

			// -------------------------------------------------------------
			// ported from aether GenericVersionTest#testEmptyVersion
			// -------------------------------------------------------------
			[ "0",           "",              0 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testEdgeCase_1_2 / 2_1 / 2_3 / 2_4
			// -------------------------------------------------------------
			[ "ga.ga.foo",     "foo",             -1 ],
			[ "0.foo.1.2.3",   "foo.1.2.3",        1 ],
			[ "0.foo",         "foo",              1 ],
			[ "1.0.0-foo",     "1-foo",            0 ],
			[ "1.0.0-ga-foo",  "1-foo",           -1 ],
			[ "1.0.0-ga-foo",  "1-ga-foo",         0 ],
			[ "1.0.0.final-foo","1-foo",          -1 ],
			[ "1.0.0.final-foo","1-final-foo",     0 ],
			[ "1.ga",          "1.0",              0 ],
			[ "ga.1",          "0.1",             -1 ],
			[ "1.ga.1",        "1.0.1",           -1 ],
			[ "1.0.final.1",   "1.0.1.final",    -1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testQualifier
			// -------------------------------------------------------------
			[ "1.0.0.a1",      "1.0.0.b1",        -1 ],
			[ "1.0.0.b1",      "1.0.0.m1",        -1 ],
			[ "1.0.0.m1",      "1.0.0.rc",        -1 ],
			[ "1.0.0.rc",      "1.0.0-SNAPSHOT",  -1 ],
			[ "1.0.0-SNAPSHOT","1.0.0",           -1 ],
			[ "1.0.0.ga",      "1.0.0.final",      0 ],
			[ "1.0.0.final",   "1.0.0.release",    0 ],
			[ "1.0.0.final",   "1.0.0.sp",        -1 ],
			[ "1.0.0",         "1.0.0.sp",        -1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testNumericOrdering
			// -------------------------------------------------------------
			[ "2",             "10",              -1 ],
			[ "1.2",           "1.10",            -1 ],
			[ "1.0.2",         "1.0.10",          -1 ],
			[ "1.0.0.2",       "1.0.0.10",        -1 ],
			[ "1.0.20101206.111434.1", "1.0.20101206.111435.1", -1 ],
			[ "1.0.20101206.111434.2", "1.0.20101206.111434.10", -1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testDelimiters
			// -------------------------------------------------------------
			[ "1.0",           "1-0",              0 ],
			[ "1.0",           "1_0",              0 ],
			[ "1.a",           "1a",               0 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testLeadingZerosAreSemanticallyIrrelevant
			// -------------------------------------------------------------
			[ "1",             "01",               0 ],
			[ "1.2",           "1.002",            0 ],
			[ "1.2.3",         "1.2.0003",         0 ],
			[ "1.2.3.4",       "1.2.3.00004",      0 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testTrailingZerosAreSemanticallyIrrelevant
			// -------------------------------------------------------------
			[ "1",             "1.0.0.0.0.0.0.0.0.0.0.0.0.0", 0 ],
			[ "1",             "1-0-0-0-0-0-0-0-0-0-0-0-0-0", 0 ],
			[ "1",             "1.0000000000000",   0 ],
			[ "1.0",           "1.0.0",             0 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testTrailingZerosBeforeQualifierAreSemanticallyIrrelevant
			// -------------------------------------------------------------
			[ "1.0_ga",        "1.0.0_ga",          0 ],
			[ "1.0-ga",        "1.0.0-ga",          0 ],
			[ "1.0.ga",        "1.0.0.ga",          0 ],
			[ "1.0ga",         "1.0.0ga",           0 ],
			[ "1.0-alpha",     "1.0.0-alpha",       0 ],
			[ "1.0.alpha",     "1.0.0.alpha",       0 ],
			[ "1.0alpha",      "1.0.0alpha",        0 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testInitialDelimiters / testConsecutiveDelimiters
			// -------------------------------------------------------------
			[ "0.1",           ".1",                0 ],
			[ "0.0.1",         "..1",               0 ],
			[ "0.1",           "-1",                0 ],
			[ "1.0.1",         "1..1",              0 ],
			[ "1.0.0.1",       "1...1",             0 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testUnlimitedNumberOfVersionComponents
			//        / testUnlimitedNumberOfDigitsInNumericComponent
			// -------------------------------------------------------------
			[ "1.0.1.2.3.4.5.6.7.8.9.0.1.2.10", "1.0.1.2.3.4.5.6.7.8.9.0.1.2.3", 1 ],
			[ "1.1234567890123456789012345678901", "1.123456789012345678901234567891", 1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testTransitionFromDigitToLetterAndViceVersaIsEquivalentToDelimiter
			// -------------------------------------------------------------
			[ "1alpha10",      "1.alpha.10",        0 ],
			[ "1alpha10",      "1-alpha-10",        0 ],
			[ "1.alpha10",     "1.alpha2",          1 ],
			[ "10alpha",       "1alpha",            1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testWellKnownQualifierOrdering
			// -------------------------------------------------------------
			[ "1-alpha1",      "1-a1",              0 ],
			[ "1-alpha",       "1-beta",           -1 ],
			[ "1-beta1",       "1-b1",              0 ],
			[ "1-beta",        "1-milestone",      -1 ],
			[ "1-milestone1",  "1-m1",              0 ],
			[ "1-milestone",   "1-rc",             -1 ],
			[ "1-rc",          "1-cr",              0 ],
			[ "1-rc",          "1-snapshot",       -1 ],
			[ "1-snapshot",    "1",                -1 ],
			[ "1",             "1-ga",              0 ],
			[ "1",             "1.ga.0.ga",         0 ],
			[ "1.0",           "1-ga",              0 ],
			[ "1",             "1-ga.ga",           0 ],
			[ "1",             "1-ga-ga",           0 ],
			[ "1",             "1-final",           0 ],
			[ "1",             "1-release",         0 ],
			[ "1",             "1-sp",             -1 ],
			[ "A.rc.1",        "A.ga.1",           -1 ],
			[ "A.sp.1",        "A.ga.1",            1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testWellKnownQualifierVersusUnknownQualifierOrdering
			// -------------------------------------------------------------
			[ "1-abc",         "1-alpha",           1 ],
			[ "1-abc",         "1-beta",            1 ],
			[ "1-abc",         "1-milestone",       1 ],
			[ "1-abc",         "1-rc",              1 ],
			[ "1-abc",         "1-snapshot",        1 ],
			[ "1-abc",         "1",                 1 ],
			[ "1-abc",         "1-sp",              1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testWellKnownSingleCharQualifiersOnlyRecognizedIfImmediatelyFollowedByNumber
			// -------------------------------------------------------------
			[ "1.0a",          "1.0",               1 ],
			[ "1.0-a",         "1.0",               1 ],
			[ "1.0.a",         "1.0",               1 ],
			[ "1.0a1",         "1.0",              -1 ],
			[ "1.0-a1",        "1.0",              -1 ],
			[ "1.0.a1",        "1.0",              -1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testUnknownQualifierOrdering
			// -------------------------------------------------------------
			[ "1-abc",         "1-abcd",           -1 ],
			[ "1-abc",         "1-bcd",            -1 ],
			[ "1-abc",         "1-aac",             1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testCaseInsensitiveOrderingOfQualifiers
			// -------------------------------------------------------------
			[ "1.alpha",       "1.ALPHA",           0 ],
			[ "1.beta",        "1.BETA",            0 ],
			[ "1.milestone",   "1.MILESTONE",       0 ],
			[ "1.rc",          "1.RC",              0 ],
			[ "1.cr",          "1.CR",              0 ],
			[ "1.snapshot",    "1.SNAPSHOT",        0 ],
			[ "1.ga",          "1.GA",              0 ],
			[ "1.final",       "1.FINAL",           0 ],
			[ "1.release",     "1.RELEASE",         0 ],
			[ "1.sp",          "1.SP",              0 ],
			[ "1.unknown",     "1.UNKNOWN",         0 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testHypenBeforeUnderscoreDotOrder
			// -------------------------------------------------------------
			[ "1.0.0-1",       "1.0.0_1",           0 ],
			[ "1.0.0-1",       "1.0.0.1",           0 ],
			[ "1.0.0_1",       "1.0.0.1",           0 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testQualifierVersusNumberOrdering
			// -------------------------------------------------------------
			[ "1-ga",          "1-1",              -1 ],
			[ "1.ga",          "1.1",              -1 ],
			[ "1-ga",          "1.0",               0 ],
			[ "1.ga",          "1.0",               0 ],
			[ "1.sp",          "1.0",               1 ],
			[ "1.sp",          "1.1",              -1 ],
			[ "1-abc",         "1-1",              -1 ],
			[ "1.abc",         "1.1",              -1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testVersionEvolution — pairwise adjacent
			// -------------------------------------------------------------
			[ "0.9.9-SNAPSHOT",    "0.9.9",              -1 ],
			[ "0.9.9",             "0.9.10-SNAPSHOT",    -1 ],
			[ "0.9.10-SNAPSHOT",   "0.9.10",             -1 ],
			[ "0.9.10",            "1.0-alpha-2-SNAPSHOT",-1 ],
			[ "1.0-alpha-2-SNAPSHOT","1.0-alpha-2",      -1 ],
			[ "1.0-alpha-2",       "1.0-alpha-10-SNAPSHOT",-1 ],
			[ "1.0-alpha-10-SNAPSHOT","1.0-alpha-10",    -1 ],
			[ "1.0-alpha-10",      "1.0-beta-1-SNAPSHOT",-1 ],
			[ "1.0-beta-1-SNAPSHOT","1.0-beta-1",        -1 ],
			[ "1.0-beta-1",        "1.0-rc-1-SNAPSHOT",  -1 ],
			[ "1.0-rc-1-SNAPSHOT", "1.0-rc-1",           -1 ],
			[ "1.0-rc-1",          "1.0-SNAPSHOT",       -1 ],
			[ "1.0-SNAPSHOT",      "1.0",                -1 ],
			[ "1.0",               "1.0-sp-1-SNAPSHOT",  -1 ],
			[ "1.0-sp-1-SNAPSHOT", "1.0-sp-1",           -1 ],
			[ "1.0-sp-1",          "1.0.1-alpha-1-SNAPSHOT",-1 ],
			[ "1.0.1",             "1.1-SNAPSHOT",       -1 ],
			[ "1.1-SNAPSHOT",      "1.1",                -1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testMinimumSegment / testMaximumSegment
			// -------------------------------------------------------------
			[ "1.min",             "1.0-alpha-1",        -1 ],
			[ "1.min",             "1.0-SNAPSHOT",       -1 ],
			[ "1.min",             "1.0",                -1 ],
			[ "1.min",             "1.MIN",               0 ],
			[ "1.min",             "0.99999",             1 ],
			[ "1.min",             "0.max",               1 ],
			[ "1.max",             "1.0-alpha-1",         1 ],
			[ "1.max",             "1.0-SNAPSHOT",        1 ],
			[ "1.max",             "1.0",                 1 ],
			[ "1.max",             "1.MAX",               0 ],
			[ "1.max",             "2.0-alpha-1",        -1 ],
			[ "1.max",             "2.min",              -1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testCompareLettersToNumbers / testCompareDigitToLetter
			// -------------------------------------------------------------
			[ "1.7",               "J",                   1 ],
			[ "7",                 "J",                   1 ],
			[ "7",                 "c",                   1 ],

			// -------------------------------------------------------------
			// aether GenericVersionTest#testLexicographicOrder
			// -------------------------------------------------------------
			[ "zebra",             "aardvark",            1 ]
		];
	}

	/**
	 * Baseline cases for Lucee's current MavenUtil.resolveVersionRange.
	 *
	 * Each row is a struct with:
	 *   input    — the version/range spec handed to resolveVersionRange
	 *   current  — what the current (broken) impl returns, or "ERROR" if it throws
	 *   verdict  — one of:
	 *                "OK"                  — current impl is correct
	 *                "WRONG-lower-bound"   — returns the bracketed lower bound literally
	 *                "WRONG-passthrough"   — doesn't start with [, returned unchanged
	 *                "WRONG-throws"        — crashes with StringIndexOutOfBoundsException
	 *                "WRONG-union-first"   — picks lower bound of first range in a union
	 *   correct  — the value a maven-correct resolver should return. For ranges
	 *              that genuinely need an available-versions list to answer
	 *              (e.g. [2.2,3)), use "NEEDS_LIST" as a sentinel.
	 */
	private array function currentResolveCases() {
		return [
			// plain versions — pass through unchanged
			{ input: "1.0",               current: "1.0",               verdict: "OK",                 correct: "1.0"        },
			{ input: "1.0-SNAPSHOT",      current: "1.0-SNAPSHOT",      verdict: "OK",                 correct: "1.0-SNAPSHOT" },
			{ input: "2.3.9",             current: "2.3.9",             verdict: "OK",                 correct: "2.3.9"      },

			// bracket forms with bounds — single-arg form can't answer without a version list,
			// so falls through to raw spec. `[2.2,3)` does NOT mean "2.2 exists"; many real
			// artifacts (jaxb-runtime et al) declare ranges where the literal lower bound
			// was never published. Correct resolution requires metadata — use the 5-arg form.
			{ input: "[2.2,3)",           current: "[2.2,3)",           verdict: "FALLTHROUGH-NEEDS-LIST", correct: "NEEDS_LIST" },
			{ input: "[1.0,2.0)",         current: "[1.0,2.0)",         verdict: "FALLTHROUGH-NEEDS-LIST", correct: "NEEDS_LIST" },
			{ input: "[1.0,)",            current: "[1.0,)",            verdict: "FALLTHROUGH-NEEDS-LIST", correct: "NEEDS_LIST" },

			// pinned — no longer throws; new impl returns the pinned version correctly
			{ input: "[1.0]",             current: "1.0",               verdict: "OK",                 correct: "1.0"        },
			// wildcard — expanded to [min,max] under the hood; bestEffort returns null, falls through raw
			{ input: "[1.2.*]",           current: "[1.2.*]",           verdict: "FALLTHROUGH-NEEDS-LIST", correct: "NEEDS_LIST" },

			// paren forms — exclusive lower bound, bestEffort returns null, falls through raw
			{ input: "(1.0,2.0]",         current: "(1.0,2.0]",         verdict: "FALLTHROUGH-NEEDS-LIST", correct: "NEEDS_LIST" },
			{ input: "(,1.0]",            current: "(,1.0]",            verdict: "FALLTHROUGH-NEEDS-LIST", correct: "NEEDS_LIST" },
			{ input: "(1.0,3.0)",         current: "(1.0,3.0)",         verdict: "FALLTHROUGH-NEEDS-LIST", correct: "NEEDS_LIST" },

			// unions — multiple subranges, bestEffort returns null, falls through raw
			{ input: "[1.0,1.5],[2.0,)",  current: "[1.0,1.5],[2.0,)",  verdict: "FALLTHROUGH-NEEDS-LIST", correct: "NEEDS_LIST" }
		];
	}

	/**
	 * pickHighest cases — exercises the metadata-driven resolution path.
	 *
	 * Each row:
	 *   spec       — the range/version spec
	 *   candidates — array of version strings as a metadata.xml would return
	 *   expected   — version string pickHighest should select
	 *                (omit the key entirely for cases that should return null)
	 */
	private array function pickCases() {
		return [
			// --- pinned --------------------------------------------------
			{ spec: "[1.0]",             candidates: [ "1.0", "1.0.1", "2.0" ],                 expected: "1.0" },
			{ spec: "[1.0]",             candidates: [ "0.9", "1.1", "2.0" ]                                   },  // pinned absent -> null
			{ spec: "1.0",               candidates: [ "1.0", "1.0.1" ],                         expected: "1.0" },

			// --- inclusive/exclusive bounds -----------------------------
			{ spec: "[1.0,2.0]",         candidates: [ "0.9", "1.0", "1.5", "2.0", "2.1" ],     expected: "2.0" },
			{ spec: "[1.0,2.0)",         candidates: [ "1.0", "1.5", "1.99", "2.0", "2.1" ],    expected: "1.99" },
			{ spec: "(1.0,2.0]",         candidates: [ "1.0", "1.0.1", "2.0", "2.1" ],          expected: "2.0" },
			{ spec: "(1.0,2.0)",         candidates: [ "1.0", "1.5", "2.0" ],                   expected: "1.5" },

			// --- open bounds --------------------------------------------
			{ spec: "[1.0,)",            candidates: [ "0.9", "1.0", "2.5", "99.0" ],           expected: "99.0" },
			{ spec: "(,1.0]",            candidates: [ "0.1", "0.9", "1.0", "1.1" ],            expected: "1.0" },

			// --- wildcard -----------------------------------------------
			{ spec: "[1.2.*]",           candidates: [ "1.1.9", "1.2.0", "1.2.7", "1.3.0" ],    expected: "1.2.7" },

			// --- union --------------------------------------------------
			{ spec: "[1.0,1.5],[2.0,)",  candidates: [ "0.9", "1.0", "1.5", "1.7", "2.5" ],     expected: "2.5" },
			{ spec: "[1.0,1.5],[2.0,)",  candidates: [ "1.6", "1.7", "1.9" ]                                    },  // gap -> null

			// --- pre-release filter active ------------------------------
			// jaxb-runtime real-world: [2.2,3) must not pick 3.0.0-M5 even though
			// per maven compareTo M5 < 3.0.0 < 3 and is technically in range.
			{ spec: "[2.2,3)",           candidates: [ "2.2.11", "2.3.9", "3.0.0-M5", "3.0.0-beta-1" ], expected: "2.3.9" },
			{ spec: "[1.0,2.0)",         candidates: [ "1.5-M1", "1.5-beta", "1.5-SNAPSHOT" ]                    },  // all pre-release -> null
			{ spec: "[1.0,2.0)",         candidates: [ "1.0-alpha", "1.5", "1.5-beta" ],        expected: "1.5" },

			// --- pre-release filter off when bound names pre-release ---
			{ spec: "[3.0.0-M1,3.0.0)",  candidates: [ "3.0.0-M1", "3.0.0-M3", "3.0.0-M5" ],    expected: "3.0.0-M5" },
			{ spec: "[1.0-beta,2.0)",    candidates: [ "1.0-beta", "1.0-beta-2", "1.5" ],       expected: "1.5" },

			// --- empty / no match ---------------------------------------
			{ spec: "[1.0,2.0)",         candidates: [ ]                                                       },
			{ spec: "[10.0,)",           candidates: [ "1.0", "2.0", "5.0" ]                                    }
		];
	}

	/**
	 * Range containment cases.
	 * Row: [ rangeSpec, version, expectedContained ]
	 */
	private array function rangeCases() {
		return [
			// -------------------------------------------------------------
			// Lucee-local smoke cases — pinned / incl / excl / open / unions
			// -------------------------------------------------------------
			[ "[1.0]",             "1.0",     true  ],
			[ "[1.0]",             "1.1",     false ],
			[ "[1.0,2.0)",         "1.0",     true  ],
			[ "[1.0,2.0)",         "1.5",     true  ],
			[ "[1.0,2.0)",         "2.0",     false ],
			[ "(1.0,2.0]",         "1.0",     false ],
			[ "(1.0,2.0]",         "2.0",     true  ],
			[ "[1.0,)",            "99.0",    true  ],
			[ "[1.0,)",            "0.9",     false ],
			[ "(,1.0]",            "0.5",     true  ],
			[ "(,1.0]",            "1.5",     false ],
			[ "[1.0,1.5],[2.0,)",  "1.2",     true  ],
			[ "[1.0,1.5],[2.0,)",  "1.7",     false ],  // gap
			[ "[1.0,1.5],[2.0,)",  "2.5",     true  ],

			// Real-world: ehcache 3.10.8's jaxb-runtime range
			[ "[2.2,3)",           "2.2",     true  ],
			[ "[2.2,3)",           "2.2.11",  true  ],
			[ "[2.2,3)",           "2.3.9",   true  ],
			[ "[2.2,3)",           "3.0",     false ],

			// -------------------------------------------------------------
			// aether GenericVersionRangeTest#testLowerBoundInclusiveUpperBoundInclusive
			// -------------------------------------------------------------
			[ "[1,2]",             "1",             true  ],
			[ "[1,2]",             "1.1-SNAPSHOT",  true  ],
			[ "[1,2]",             "2",             true  ],

			// -------------------------------------------------------------
			// aether GenericVersionRangeTest#testLowerBoundInclusiveUpperBoundExclusive
			// -------------------------------------------------------------
			[ "[1.2.3.4.5,1.2.3.4.6)", "1.2.3.4.5", true  ],
			[ "[1.2.3.4.5,1.2.3.4.6)", "1.2.3.4.6", false ],

			// -------------------------------------------------------------
			// aether GenericVersionRangeTest#testLowerBoundExclusiveUpperBoundInclusive
			// -------------------------------------------------------------
			[ "(1a,1b]",           "1a",            false ],
			[ "(1a,1b]",           "1b",            true  ],

			// -------------------------------------------------------------
			// aether GenericVersionRangeTest#testLowerBoundExclusiveUpperBoundExclusive
			// -------------------------------------------------------------
			[ "(1,3)",             "1",             false ],
			[ "(1,3)",             "2-SNAPSHOT",    true  ],
			[ "(1,3)",             "3",             false ],

			// -------------------------------------------------------------
			// aether GenericVersionRangeTest#testSingleVersion
			// -------------------------------------------------------------
			[ "[1]",               "1",             true  ],
			[ "[1,1]",             "1",             true  ],

			// -------------------------------------------------------------
			// aether GenericVersionRangeTest#testSingleWildcardVersion
			// -------------------------------------------------------------
			[ "[1.2.*]",           "1.2-alpha-1",   true  ],
			[ "[1.2.*]",           "1.2-SNAPSHOT",  true  ],
			[ "[1.2.*]",           "1.2",           true  ],
			[ "[1.2.*]",           "1.2.9999999",   true  ],
			[ "[1.2.*]",           "1.3-rc-1",      false ]
		];
	}
}
