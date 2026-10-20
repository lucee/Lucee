component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1467", skip=!perlAvailable(), body=function() {
			it(title="Checking REMatch() with line breaks ", body = function( currentSpec ) {
				var REMatchTest = ArrayLen( REMatch( '.', Chr( 10 ) ) );
				expect(REMatchTest).toBe(1);
			});
			it(title="Checking REFind() with line breaks", body = function( currentSpec ) {
				var REFindTest = REFind( '.',Chr( 10 ) ) ;
				expect(REFindTest).toBe(1);
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
