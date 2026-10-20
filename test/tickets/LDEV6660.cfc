component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function afterAll() {
		application action="update" regex={ type: "perl" };
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6660 - perl regex engine is deprecated as default", function() {

			it( title="perl is detected as the deprecated engine", body=function() {
				application action="update" regex={ type: "perl" };
				expect( reFind( "b", "abc" ) ).toBe( 2 );
				expect( getRegexDeprecation().isPerl( getPageContext().getRegex() ) ).toBeTrue();
			});

			it( title="the warning is logged once per application, not per call", body=function() {
				application action="update" regex={ type: "perl" };
				var rd = getRegexDeprecation();
				var pc = getPageContext();
				var regex = pc.getRegex();
				// the first regex call of this request already triggered the check
				expect( rd.hasWarned( pc ) ).toBeTrue();
				expect( rd.warnIfPerl( pc, regex ) ).toBeFalse();
				expect( rd.warnIfPerl( pc, regex ) ).toBeFalse();
			});

			it( title="java and compat are not deprecated", body=function() {
				application action="update" regex={ type: "java" };
				var rd = getRegexDeprecation();
				expect( rd.isPerl( getPageContext().getRegex() ) ).toBeFalse();
				expect( rd.warnIfPerl( getPageContext(), getPageContext().getRegex() ) ).toBeFalse();
				application action="update" regex={ type: "compat" };
				expect( rd.isPerl( getPageContext().getRegex() ) ).toBeFalse();
			});

			it( title="the regexType schema description marks perl as deprecated", body=function() {
				var prop = createObject( "java", "lucee.runtime.config.ConfigServerImpl" ).metaRegex;
				var desc = createObject( "java", "lucee.runtime.regex.RegexFactory" ).getInstance().schema( prop ).description;
				expect( desc ).toInclude( "Deprecated" );
				expect( desc ).toInclude( "Lucee 9" );
			});
		});
	}

	private function getRegexDeprecation() {
		return createObject( "java", "lucee.runtime.regex.RegexDeprecation" );
	}
}
