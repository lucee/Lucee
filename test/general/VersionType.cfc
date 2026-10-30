component extends="org.lucee.cfml.test.LuceeTestCase" labels="version" {

	function beforeAll() {
		variables.VersionClass = createObject( "java", "lucee.loader.util.VersionImpl" );
	}

	private function v( required string version ) {
		return VersionClass.parseVersion( arguments.version, VersionClass.emptyVersion );
	}

	private function strict( required string version ) {
		return VersionClass.parse( arguments.version, true, VersionClass.emptyVersion );
	}

	private numeric function cmp( required string left, required string right ) {
		return sgn( v( arguments.left ).compareTo( v( arguments.right ) ) );
	}

	function run( testResults, testBox ) {
		describe( "the version type (LDEV-6627)", function() {

			it( "parses the parts of a Lucee version", function() {
				var version = v( "9.0.0.12-ALPHA" );
				expect( version.getMajor() ).toBe( 9 );
				expect( version.getMinor() ).toBe( 0 );
				expect( version.getMicro() ).toBe( 0 );
				expect( version.getBuild() ).toBe( 12 );
				expect( version.getQualifier() ).toBe( "ALPHA" );
				expect( version.toString() ).toBe( "9.0.0.12-ALPHA" );
			});

			it( "a part that is not there is 0, a release has no qualifier", function() {
				var version = v( "7.1" );
				expect( [ version.getMajor(), version.getMinor(), version.getMicro(), version.getBuild() ] ).toBe( [ 7, 1, 0, 0 ] );
				expect( version.getQualifier() ).toBe( "" );
				expect( v( "8.0.0.5" ).getQualifier() ).toBe( "" );
			});

			it( "strict follows the rules of OSGi: every part that is there must be a number", function() {
				expect( strict( "9.0.0.12-ALPHA" ).toString() ).toBe( "9.0.0.12-ALPHA" );
				expect( strict( "1.2.3" ).toString() ).toBe( "1.2.3" );
				expect( strict( "abc" ).toString() ).toBe( "" );
				expect( strict( "1.x.3" ).toString() ).toBe( "" );
				expect( strict( "" ).toString() ).toBe( "" );
			});

			it( "lenient accepts what Maven accepts", function() {
				expect( v( "abc" ).toString() ).toBe( "abc" );
				expect( v( "1.x.3" ).toString() ).toBe( "1.x.3" );
				expect( v( "2.5.2-BETA" ).getQualifier() ).toBe( "BETA" );
			});

			it( "the parse that throws gives a message with the pattern", function() {
				expect( function() {
					VersionClass.parse( "1.x.3", true );
				}).toThrow();
				expect( VersionClass.parse( "1.x.3", false ).toString() ).toBe( "1.x.3" );
			});

			it( "the build is compared as a number", function() {
				expect( cmp( "9.0.0.9", "9.0.0.10" ) ).toBe( -1 );
				expect( cmp( "9.0.0.100", "9.0.0.99" ) ).toBe( 1 );
				expect( cmp( "7.0.0.159", "7.0.0.159" ) ).toBe( 0 );
				expect( cmp( "10.0.0.0", "9.99.99.99" ) ).toBe( 1 );
			});

			it( "a release is newer than a version with a qualifier, SNAPSHOT is the oldest", function() {
				expect( cmp( "9.0.0.1", "9.0.0.1-RC" ) ).toBe( 1 );
				expect( cmp( "9.0.0.1-RC", "9.0.0.1-BETA" ) ).toBe( 1 );
				expect( cmp( "9.0.0.1-BETA", "9.0.0.1-ALPHA" ) ).toBe( 1 );
				expect( cmp( "9.0.0.1-ALPHA", "9.0.0.1-SNAPSHOT" ) ).toBe( 1 );
				expect( cmp( "9.0.0.1-SNAPSHOT", "9.0.0.1" ) ).toBe( -1 );
				// a higher number always wins
				expect( cmp( "9.0.0.2-SNAPSHOT", "9.0.0.1" ) ).toBe( 1 );
			});

			it( "qualifiers are compared case-insensitive", function() {
				expect( cmp( "1.0.0.0-rc", "1.0.0.0-RC" ) ).toBe( 0 );
				expect( v( "1.0.0.0-rc" ).equals( v( "1.0.0.0-RC" ) ) ).toBeTrue();
			});

			it( "versions that are the same are equal and have the same hash code", function() {
				expect( v( "1.0" ).equals( v( "1.0.0.0" ) ) ).toBeTrue();
				expect( v( "1.0" ).hashCode() ).toBe( v( "1.0.0.0" ).hashCode() );
				expect( v( "1.0" ).equals( v( "1.0.0.1" ) ) ).toBeFalse();
			});

			it( "is(SNAPSHOT) ignores the case of the qualifier, like the ordering", function() {
				expect( v( "1.0.0-snapshot" ).is( VersionClass.SNAPSHOT ) ).toBeTrue();
				expect( v( "1.0.0-snapshot" ).is( VersionClass.RELEASE ) ).toBeFalse();
				expect( v( "1.0.0.5-SNAPSHOT" ).is( VersionClass.SNAPSHOT ) ).toBeTrue();
				expect( v( "1.0.0" ).is( VersionClass.RELEASE ) ).toBeTrue();
			});

			it( "the loader ignores a name that is not a version (strict toVersion)", function() {
				var support = createObject( "java", "lucee.loader.engine.CFMLEngineFactorySupport" );
				var zero = VersionClass.parseVersion( "0.0.0.0" );
				expect( support.toVersion( "foo", zero ).toString() ).toBe( "0.0.0.0" );
				expect( support.toVersion( "abc.lco", zero ).toString() ).toBe( "0.0.0.0" );
				expect( support.toVersion( "7.1.2.200-SNAPSHOT.lco", zero ).toString() ).toBe( "7.1.2.200-SNAPSHOT" );
			});

			it( "a version with a qualifier survives the round trip through OSGi", function() {
				var osgiUtil = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
				loop list="1.0.0-SNAPSHOT,9.0.0.12-ALPHA,7.1.2.200-SNAPSHOT,7.1.2.200,1.2.3" item="local.str" {
					var back = osgiUtil.toLucee( osgiUtil.toOSGi( v( str ) ) );
					expect( back.compareTo( v( str ) ) ).toBe( 0, str );
				}
			});

			it( "the version of this Lucee is a version of this type", function() {
				var info = getPageContext().getConfig().getFactory().getEngine().getInfo();
				var version = info.getVersion();
				expect( version.getClass().getName() ).toBe( "lucee.loader.util.VersionImpl" );
				expect( version.toString() ).toBe( server.lucee.version );
				expect( version.getMajor() ).toBeGTE( 9 );
			});

			it( "server.lucee.state is the qualifier of the version, or stable", function() {
				var version = VersionClass.parseVersion( server.lucee.version, VersionClass.emptyVersion );
				var expected = len( version.getQualifier() ) ? version.getQualifier() : "stable";
				expect( server.lucee.state ).toBe( expected );
			});
		});
	}
}
