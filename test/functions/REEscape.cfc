component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeAll(){
		variables.samples = {
			  "lucee": "lucee"
			, "lucee?[]^": "lucee\?\[\]\^"
			, "/s/acf/lucee":"\/s\/acf\/lucee"
			, "Hello. How are you? (test) [123] {abc} ^$.*+?|\": "Hello\.\ How\ are\ you\?\ \(test\)\ \[123\]\ \{abc\}\ \^\$\.\*\+\?\|\\"
			, "abcdefghijklmnopqrstuvwxyz0123456789":"abcdefghijklmnopqrstuvwxyz0123456789"
			, "1": "1"
			, "": ""
			, "\Q\E":"\\Q\\E"
			, "\Qabc\E":"\\Qabc\\E"
			, " ": "\ "
		};
		// edge cases
		variables.origRegex = getApplicationSettings().regex;
		variables.perlSamples = {};
		// For java Lucee manually escapes as Pattern.quote simple wraps a string with \Q and \E
		variables.javaSamples = {};
	};

	function afterAll(){
			application action="update" regex=variables.origRegex;
	};

	// the perl engine is provided by the Perl Regex extension
	private boolean function isPerlAvailable() {
		var orig = getApplicationSettings().regex;
		try {
			application action="update" regex={type="perl"};
			reFind( "a", "a" );
			return true;
		}
		catch ( any e ) {
			return false;
		}
		finally {
			application action="update" regex=orig;
		}
	}

	function run( testResults , testBox ) {
		describe( title="Testcase for reReplace - java", body=function() {
			beforeAll( function( currentSpec, data ){
				application regex={type="java"};
			});

			it(title="Checking with reEscape function BIF - java", body=function( currentSpec ) {
				test( "java" );
			});

			it(title="Checking with string.reEscape() member function - java", body=function( currentSpec ) {
				testMember( "java" );
			});
		});

		describe( title="Testcase for reEscape - member - perl", skip=!isPerlAvailable(), body=function() {
			beforeAll( function( currentSpec, data ){
				application regex={type="perl"};
			});

			it(title="Checking with reEscape function BIF - perl", body=function( currentSpec ) {
				test( "perl" );
			});

			it(title="Checking with string.reEscape() member function - perl ", body=function( currentSpec ) {
				testMember( "perl" );
			});
		});
	}

	private function test( type ){
		var _samples = duplicate(samples);
		structAppend( _samples, variables["#type#samples"], true );

		loop collection="#_samples#" key="local.input" value="local.expected" {
			expect( REEscape( input ) ).toBe( expected );
		}
	}

	private function testMember( type ){
		var _samples = duplicate(samples);
		structAppend( _samples, variables["#type#samples"], true );

		loop collection="#_samples#" key="local.input" value="local.expected" {
			expect( input.REEscape() ).toBe( expected );
		}
	}
}