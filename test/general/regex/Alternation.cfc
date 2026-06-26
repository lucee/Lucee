// Alternation (|) and non-capturing groups (?:...).
// Both documented in the Adobe ColdFusion regex reference.
// Non-capturing groups (?:...) group without adding to the backref count —
// \1 skips them and refers to the next capturing group.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	public void function testBasicAlternation() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "cat" ], reMatch( "cat|dog", "there is a cat here" ) );
			assertEquals( [ "dog" ], reMatch( "cat|dog", "there is a dog here" ) );
		});
	}

	public void function testAlternationScopeAll() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "cat", "dog" ], reMatch( "cat|dog", "cat and dog" ) );
		});
	}

	public void function testAlternationInGroup() {
		variables._regex.eachEngine( function( engine ) {
			// (cat|dog)s? — reMatch returns the full match, not the group contents
			assertEquals( [ "cats", "dog" ], reMatch( "(cat|dog)s?", "cats and dog" ) );
		});
	}

	public void function testAlternationReplace() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "I see animals and animals", reReplace( "I see cats and dogs", "cats|dogs", "animals", "all" ) );
		});
	}

	// jelly|jellies — alternation tries left branch first; order matters
	public void function testAlternationOrderMatters() {
		variables._regex.eachEngine( function( engine ) {
			// "jelly" matches first branch before "jellies" can be tried
			assertEquals( [ "jelly" ], reMatch( "jelly|jellies", "I like jelly" ) );
			assertEquals( [ "jellies" ], reMatch( "jellies|jelly", "I like jellies" ) );
		});
	}

	// (?:...) non-capturing group — does not consume a backref slot
	public void function testNonCapturingGroupDoesNotConsumeBackref() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// pattern has one capturing group (\w+); (?:Hello) is non-capturing so \1 = "Bond"
			assertEquals( "Bond", reReplace( "Hello Bond", "(?:Hello) (\w+)", "\1" ) );
		});
	}

	// contrast: with a capturing group, \1 = "Hello" and \2 = "Bond"
	public void function testCapturingGroupConsumesBackref() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "Bond Hello", reReplace( "Hello Bond", "(Hello) (\w+)", "\2 \1" ) );
		});
	}

	public void function testNonCapturingGroupWithAlternation() unsupportedRegexEngine="java" {
		variables._regex.eachEngine( function( engine ) {
			// (?:Hi|Hello) matches the greeting without capturing it; \1 = surname
			assertEquals( "Bond", reReplace( "Hello Bond", "(?:Hi|Hello) (\w+)", "\1" ) );
			assertEquals( "Bond", reReplace( "Hi Bond",    "(?:Hi|Hello) (\w+)", "\1" ) );
		});
	}
}
