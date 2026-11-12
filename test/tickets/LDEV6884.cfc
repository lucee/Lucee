component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function beforeAll() {
		// there is no CFML function to reload the function library, so the config is used directly
		variables.configServer = createObject( "java", "lucee.runtime.config.ConfigUtil" )
			.getConfigServerImpl( getPageContext().getConfig() );
		variables.srcFld = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6884/ldev6884-member-cache-test.fld";
		variables.fldPath = configServer.getFldFile().getRealResource( "ldev6884-member-cache-test.fld" ).getAbsolutePath();
	}

	function afterAll() {
		removeFld();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6884 member functions after resetFLDs()", function() {

			it( "a member function added through an FLD is callable as a member", function() {
				var s = "abc";
				s.uCase(); // make sure the string member map has been built

				deployFld();

				expect( structKeyExists( getFunctionList(), "ldev6884MemberCacheTest" ) ).toBeTrue();
				expect( s.ldev6884MemberCacheTest() ).toBe( "ABC" );

				removeFld();
			});

			it( "a member function removed with its FLD is no longer callable as a member", function() {
				var s = "abc";
				deployFld();
				expect( s.ldev6884MemberCacheTest() ).toBe( "ABC" );

				removeFld();

				expect( structKeyExists( getFunctionList(), "ldev6884MemberCacheTest" ) ).toBeFalse();
				expect( function() {
					s.ldev6884MemberCacheTest();
				}).toThrow();
			});

		});
	}

	private function deployFld() {
		fileCopy( variables.srcFld, variables.fldPath );
		variables.configServer.resetFLDs();
	}

	private function removeFld() {
		if ( fileExists( variables.fldPath ) ) fileDelete( variables.fldPath );
		variables.configServer.resetFLDs();
	}
}
