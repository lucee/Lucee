component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title="test suite for LDEV3066", skip=!perlAvailable(), body=function() {
			it(title = "test reReplace", body = function( currentSpec ) {
				var string = '---abc---asdasd';
				var regex = '---(.*)---(.*)';
				
				expect("abc").toBe(ReReplace( string, regex, "\1" ));
				expect("asdasd").toBe(ReReplace( string, regex, "\2" ));
				expect("").toBe(ReReplace( string, regex, "\3" ));
			});


			it(title = "test reFind", body = function( currentSpec ) {
				var string = '---abc---asdasd';
				var regex = '---(.*)---(.*)';
				var res=ReFind( regex, string, 1, true );
				
				expect("15,3,6").toBe(arrayToList(res.len));
				expect("1,4,10").toBe(arrayToList(res.pos));
				expect("---abc---asdasd,abc,asdasd").toBe(arrayToList(res.match));
			});

			it(title = "test reFind", body = function( currentSpec ) {
				var string = '---abc---asdasd';
				var regex = '^---(.*)---(.*)$';
				var res=ReFind( regex, string, 1, true );
				
				expect("15,3,6").toBe(arrayToList(res.len));
				expect("1,4,10").toBe(arrayToList(res.pos));
				expect("---abc---asdasd,abc,asdasd").toBe(arrayToList(res.match));
			});

		});
	}
	// perl engine semantics, the engine is provided by the Perl Regex extension (LDEV-6632)
	private boolean function perlAvailable() {
		var orig = getApplicationSettings().regex;
		try {
			application action="update" regex={ type: "perl" };
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

	function beforeEach() {
		variables._origRegex = getApplicationSettings().regex;
		application action="update" regex={ type: "perl" };
	}

	function afterEach() {
		application action="update" regex=variables._origRegex;
	}
}
