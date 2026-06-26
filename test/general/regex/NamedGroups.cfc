// Named groups: (?<name>...) capture and \k<name> backref in patterns and replacements.
// Pattern-side named groups are java+compat only — perl (Oro) doesn't support them.
// Replacement-side \k<name> is compat only.
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	public void function testNamedGroupCapture() unsupportedRegexEngine="perl" {
		variables._regex.eachEngine( function( engine ) {
			// captures "world" via the named group; reMatch returns whole matches only
			assertEquals( [ "world" ], reMatch( "(?<greeting>world)", "hello world" ) );
		});
	}

	public void function testNamedBackref() unsupportedRegexEngine="perl" {
		variables._regex.eachEngine( function( engine ) {
			// match repeated word — uses \k<name> backref to the captured group
			var r = reMatch( "\b(?<w>\w+) \k<w>\b", "the cat cat and the dog" );
			assertEquals( [ "cat cat" ], r );
		});
	}

	public void function testNamedGroupBackrefInReplacement() unsupportedRegexEngine="java,perl" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "hello! world", reReplace( "hello world", "(?<word>\w+)", "\k<word>!" ) );
		});
	}

	public void function testNamedGroupBackrefInReplacementAll() unsupportedRegexEngine="java,perl" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "[hello] [world]", reReplace( "hello world", "(?<word>\w+)", "[\k<word>]", "all" ) );
		});
	}

	public void function testNamedGroupBackrefWithCaseModifier() unsupportedRegexEngine="java,perl" {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( "Hello world", reReplace( "hello world", "(?<word>\w+)", "\u\k<word>" ) );
		});
	}

	public void function testNamedGroupBackrefMissingCloseThrows() unsupportedRegexEngine="java,perl" {
		variables._regex.eachEngine( function( engine ) {
			var threw = false;
			try {
				reReplace( "hello", "(?<word>\w+)", "\k<word" );
			} catch ( any e ) {
				threw = true;
			}
			assertTrue( threw, "expected exception for unclosed \k<name>" );
		});
	}

	public void function testNamedGroupBackrefNonexistentGroupThrows() unsupportedRegexEngine="java,perl" {
		variables._regex.eachEngine( function( engine ) {
			var threw = false;
			try {
				reReplace( "hello", "(?<word>\w+)", "\k<nope>" );
			} catch ( any e ) {
				threw = true;
			}
			assertTrue( threw, "expected exception for unknown group name" );
		});
	}
}
