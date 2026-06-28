// Non-ASCII / locale-sensitivity tests for \u \l \U \L case modifiers.
// ACF probe (acf-regex-6432/result-locale.txt) confirmed ACF uses per-char
// Character.toUpperCase/toLowerCase (not String.toUpperCase/toLowerCase), so:
//   - \U on ß stays ß (no SS expansion)
//   - \L on İ (U+0130) gives i (simple mapping, not i + combining dot U+0307)
//   - locale has no effect (i -> I, never İ under Turkish JVM locale)
// java raw engine excluded — it uses translateReplacement, not expandReplacement.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	// ß (U+00DF): String.toUpperCase() -> "SS", Character.toUpperCase('ß') -> 'ß'
	// ACF returns "ß" — confirms per-char mapping, not multi-char expansion
	public void function testUpperGermanSharpSStaysSharpS() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( chr(223), reReplace( chr(223), "(.+)", "\U\1" ) );
		});
	}

	// ü (U+00FC) -> Ü (U+00DC): same result for both per-char and String.toUpperCase
	public void function testUpperGermanUmlautU() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( chr(220), reReplace( chr(252), "(.)", "\U\1" ) );
		});
	}

	// İ (U+0130, capital I with dot): String.toLowerCase(ROOT) -> "i̇", Character.toLowerCase -> 'i'
	// ACF returns "i" — confirms per-char simple mapping
	public void function testLowerCapitalIWithDotGivesPlainI() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "i", reReplace( chr(304), "(.)", "\L\1" ) );
		});
	}

	// ı (U+0131, dotless i): uppercase -> I (same in ROOT and Turkish locale)
	public void function testUpperDotlessIGivesI() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "I", reReplace( chr(305), "(.)", "\U\1" ) );
		});
	}

	// mixed ASCII + non-ASCII: "café" -> "CAFÉ"
	public void function testUpperMixedAsciiAndAccented() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "CAF" & chr(201), reReplace( "caf" & chr(233), "(.+)", "\U\1" ) );
		});
	}

	// \u (next-char only) on ß: only first char uppercased, ß stays ß
	public void function testNextUpperOnSharpSStaysSharpS() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( chr(223), reReplace( chr(223), "(.)", "\u\1" ) );
		});
	}

}
