component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function run( testResults, testBox ) {
		describe( "LDEV-6746: maven version parsing of extension versions with a non numeric 4th part", function() {

			it( "parses a version with a non numeric 4th part (e.g. mssql 13.4.0.jre11)", function() {
				var v = toVersion( "13.4.0.jre11" );
				expect( v.toString() ).toBe( "13.4.0.jre11" );
				expect( v.is( versionClass().RELEASE ) ).toBeTrue();
				expect( v.is( versionClass().SNAPSHOT ) ).toBeFalse();
			});

			it( "returns a version and not the default value for a non numeric 4th part", function() {
				var v = versionClass().parseVersion( "13.4.0.jre11", nullValue() );
				expect( isNull( v ) ).toBeFalse();
				expect( v.toString() ).toBe( "13.4.0.jre11" );
			});

			it( "detects snapshots and releases", function() {
				var V = versionClass();
				expect( toVersion( "7.1.3.22-SNAPSHOT" ).is( V.SNAPSHOT ) ).toBeTrue();
				expect( toVersion( "7.1.3.22-SNAPSHOT" ).is( V.RELEASE ) ).toBeFalse();
				expect( toVersion( "7.1.3.22" ).is( V.RELEASE ) ).toBeTrue();
				expect( toVersion( "7.1.3.22-RC" ).is( V.RELEASE ) ).toBeTrue();
				expect( toVersion( "1.0.0-SNAPSHOT" ).is( V.SNAPSHOT ) ).toBeTrue();
			});

			it( "keeps the numeric ordering of the build number", function() {
				var V = versionClass();
				expect( V.compare( toVersion( "7.1.3.9" ), toVersion( "7.1.3.22" ) ) ).toBeLT( 0 );
				expect( V.compare( toVersion( "7.1.3.22" ), toVersion( "7.1.3.9" ) ) ).toBeGT( 0 );
				expect( V.compare( toVersion( "7.1.10.1" ), toVersion( "7.1.9.1" ) ) ).toBeGT( 0 );
				expect( V.compare( toVersion( "13.4.0.jre11" ), toVersion( "13.4.0.jre11" ) ) ).toBe( 0 );
				expect( V.compare( toVersion( "12.2.0.jre8" ), toVersion( "13.4.0.jre11" ) ) ).toBeLT( 0 );
			});

			it( "keeps the original string representation", function() {
				expect( toVersion( "2.5" ).toString() ).toBe( "2.5" );
				expect( toVersion( "3.0.0.2" ).toString() ).toBe( "3.0.0.2" );
				expect( toVersion( "7.1.3.22-SNAPSHOT" ).toString() ).toBe( "7.1.3.22-SNAPSHOT" );
			});
		});
	}

	private function versionClass() {
		return createObject( "java", "lucee.loader.util.VersionImpl" );
	}

	private function toVersion( required string version ) {
		return versionClass().parseVersion( arguments.version );
	}
}
