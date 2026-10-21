component extends="org.lucee.cfml.test.LuceeTestCase" labels="system" {

	function beforeAll() {
		variables.System = createObject( "java", "java.lang.System" );
		variables.SystemUtil = createObject( "java", "lucee.commons.io.SystemUtil" );
		variables.propName = "lucee.ldev5425.test." & lCase( left( replace( createUUID(), "-", "", "all" ), 10 ) );
	}

	function afterAll() {
		System.clearProperty( variables.propName );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5425 getSystemPropOrEnvVar with an empty value", function() {

			it( title="an empty system property is returned, not the default", body=function() {
				System.setProperty( propName, "" );
				expect( SystemUtil.getSystemPropOrEnvVar( propName, "theDefault" ) ).toBe( "" );
			});

			it( title="a set system property is returned", body=function() {
				System.setProperty( propName, "theValue" );
				expect( SystemUtil.getSystemPropOrEnvVar( propName, "theDefault" ) ).toBe( "theValue" );
			});

			it( title="a missing system property returns the default", body=function() {
				System.clearProperty( propName );
				expect( SystemUtil.getSystemPropOrEnvVar( propName, "theDefault" ) ).toBe( "theDefault" );
			});

			it( title="getSystemPropOrEnvVar() returns an empty system property as empty string", body=function() {
				System.setProperty( propName, "" );
				expect( getSystemPropOrEnvVar( propName ) ).toBe( "" );
				System.setProperty( propName, "x" );
				expect( getSystemPropOrEnvVar( propName ) ).toBe( "x" );
			});
		});
	}
}
