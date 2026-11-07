component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	function run( testResults, testBox ) {
		describe( "VersionImpl parsing, conversion and comparison", function() {

			var impl = createObject( "java", "lucee.loader.util.VersionImpl" );
			var po = function( v ) { return impl.parseOSGi( v, javaCast( "null", "" ) ); };
			var pl = function( v ) { return impl.parse( v, false, javaCast( "null", "" ) ); };

			it( "parseOSGi keeps the text of every valid version", function() {
				var all = [ "1", "1.2", "1.2.3", "1.2.3.4", "1.2.3.0006L", "0.0.0", "12.2.0.jre8", "5.2.0.RELEASE", "2.0.0.Final",
					"1.0.0.v20200101", "1.0.0.SNAPSHOT", "1.0.0.20240101123456", "9.0.0.5-ALPHA", "3.12.13.10", "4.1.0.1-RC", "2.7.2-jdk8", "1.0.0.a_b-c" ];
				for ( var v in all ) {
					var r = po( v );
					expect( isNull( r ) ).toBeFalse( "[#v#] should parse" );
					expect( r.toString() ).toBe( v );
				}
			});

			it( "parseOSGi rejects invalid versions", function() {
				for ( var v in [ "abc", "", " ", "1.a.0", "1.0.0.a b", "1.0.0.1.2", "-1.0.0", "1..2", ".1", "1.", "1.2.3.4.5", "v1.0.0" ] ) {
					expect( isNull( po( v ) ) ).toBeTrue( "[#v#] should not parse" );
				}
			});

			it( "parseOSGi(String) throws for an invalid version", function() {
				expect( function() { impl.parseOSGi( "1.0.0.a b" ); } ).toThrow( "java.io.IOException" );
			});

			it( "exposes major, minor and micro", function() {
				var v = po( "12.2.7.jre8" );
				expect( v.getMajor() ).toBe( 12 );
				expect( v.getMinor() ).toBe( 2 );
				expect( v.getMicro() ).toBe( 7 );
				var s = po( "5" );
				expect( s.getMajor() ).toBe( 5 );
				expect( s.getMinor() ).toBe( 0 );
				expect( s.getMicro() ).toBe( 0 );
			});

			it( "parse accepts Lucee style versions", function() {
				for ( var v in [ "7.1.0.12", "7.1.0.12-SNAPSHOT", "6.2.1.3-RC", "7.0.0.1-ALPHA", "9.0.0.5-BETA", "5.4.8.1" ] ) {
					expect( isNull( pl( v ) ) ).toBeFalse( "[#v#] should parse" );
				}
			});

			it( "orders plain numeric versions numerically", function() {
				var chain = [ "0.0.1", "0.1.0", "1.0.0", "1.0.1", "1.2.0", "1.10.0", "2.0.0", "9.0.0", "10.0.0", "100.0.0" ];
				assertAscending( chain, po );
			});

			it( "orders four part versions numerically", function() {
				assertAscending( [ "1.0.0.1", "1.0.0.2", "1.0.0.9", "1.0.0.10", "1.0.0.100", "1.0.1.0", "1.1.0.0" ], po );
			});

			it( "orders versions with a qualifier by the numbers first, a release is newer than a qualified one", function() {
				assertAscending( [ "12.2.0.jre8", "12.9.0.jre8", "12.10.0.jre8", "12.10.0", "13.0.0.jre8" ], po );
				assertAscending( [ "5.2.0.RELEASE", "5.10.0.Final", "6.0.0.RELEASE" ], po );
				assertAscending( [ "1.0.0.20240101123456", "1.0.0", "1.1.0.20250101000000", "1.1.0" ], po );
			});

			it( "orders Lucee style versions including the suffix", function() {
				assertAscending( [ "7.0.0.9", "7.0.0.10", "7.0.1.0", "7.1.0.1", "8.0.0.0" ], pl );
				expect( impl.compare( pl( "7.1.0.12-SNAPSHOT" ), pl( "7.1.0.12" ) ) ).toBeLT( 0 );
				expect( impl.compare( pl( "7.1.0.12-ALPHA" ), pl( "7.1.0.12-BETA" ) ) ).toBeLT( 0 );
				expect( impl.compare( pl( "7.1.0.12-BETA" ), pl( "7.1.0.12-RC" ) ) ).toBeLT( 0 );
				expect( impl.compare( pl( "7.1.0.12-RC" ), pl( "7.1.0.12" ) ) ).toBeLT( 0 );
			});

			it( "compare is symmetric, reflexive and treats equal versions as equal", function() {
				for ( var v in [ "1.2.3", "12.2.0.jre8", "1.0.0.SNAPSHOT", "3.12.13.10", "4.1.0.1-RC" ] ) {
					expect( impl.compare( po( v ), po( v ) ) ).toBe( 0 );
				}
				var a = po( "1.2.3" );
				var b = po( "1.2.4" );
				expect( sgn( impl.compare( a, b ) ) ).toBe( -sgn( impl.compare( b, a ) ) );
			});

			it( "dot and hyphen form of the qualifier are the same version", function() {
				for ( var pair in [ [ "12.2.0.jre8", "12.2.0-jre8" ], [ "2.7.2.jdk8", "2.7.2-jdk8" ], [ "1.0.0.RELEASE", "1.0.0-RELEASE" ] ] ) {
					expect( impl.compare( po( pair[ 1 ] ), po( pair[ 2 ] ) ) ).toBe( 0, "#pair[ 1 ]# vs #pair[ 2 ]#" );
				}
			});

			it( "a version with trailing zero parts equals the shorter one", function() {
				expect( impl.compare( po( "1" ), po( "1.0.0" ) ) ).toBe( 0 );
				expect( impl.compare( po( "1.2" ), po( "1.2.0" ) ) ).toBe( 0 );
			});

			// TODO LDEV-6627: skipped until the OSGi extension exists, OSGiUtil moved to the extension.
			it( title="toOSGi and toLucee round trip", skip=true, body=function() {
				var osgi = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
				for ( var v in [ "1.2.3", "3.12.13.10", "12.2.0.jre8", "5.2.0.RELEASE", "1.0.0.SNAPSHOT", "1.0.0.20240101123456" ] ) {
					var l = po( v );
					var back = osgi.toLucee( osgi.toOSGi( l ) );
					expect( impl.compare( l, back ) ).toBe( 0, "round trip of #v#" );
					expect( replace( back.toString(), "-", ".", "all" ) ).toBe( replace( l.toString(), "-", ".", "all" ), "string of #v#" );
				}
			});

			// TODO LDEV-6627: skipped until the OSGi extension exists, OSGiUtil moved to the extension.
			it( title="toOSGi gives the OSGi version with the same ordering", skip=true, body=function() {
				var osgi = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
				var names = [ "12.2.0.jre8", "12.9.0.jre8", "12.10.0.jre8", "13.0.0.jre8" ];
				for ( var i = 1; i < arrayLen( names ); i++ ) {
					expect( osgi.toOSGi( po( names[ i ] ) ).compareTo( osgi.toOSGi( po( names[ i + 1 ] ) ) ) ).toBeLT( 0 );
				}
			});

			it( "VersionRange checks a version against single versions and ranges", function() {
				var cls = createObject( "java", "lucee.runtime.osgi.VersionRange" );
				var v = function( x ) { return impl.parseOSGi( x, javaCast( "null", "" ) ); };
				var single = cls.init( "6.2.0.5" );
				expect( single.isWithin( v( "6.2.0.5" ) ) ).toBeTrue();
				expect( single.isWithin( v( "7.0.0.0" ) ) ).toBeTrue();
				expect( single.isWithin( v( "6.2.0.4" ) ) ).toBeFalse();
				expect( single.isWithin( v( "6.10.0.0" ) ) ).toBeTrue();
				var range = cls.init( "6.0.0.0-7.0.0.0" );
				expect( range.isWithin( v( "6.5.0.1" ) ) ).toBeTrue();
				expect( range.isWithin( v( "5.9.0.9" ) ) ).toBeFalse();
				expect( range.isWithin( v( "7.0.0.1" ) ) ).toBeFalse();
				var list = cls.init( "5.0.0.0-5.9.0.0,7.0.0.0" );
				expect( list.isWithin( v( "5.4.0.0" ) ) ).toBeTrue();
				expect( list.isWithin( v( "7.1.0.0" ) ) ).toBeTrue();
				expect( list.isWithin( v( "6.0.0.0" ) ) ).toBeFalse();
			});

			it( "BundleDefinition equality uses the version value", function() {
				var cls = createObject( "java", "lucee.runtime.osgi.BundleDefinition" );
				expect( cls.init( "a.b", "2.7.2-jdk8" ).equals( cls.init( "a.b", "2.7.2.jdk8" ) ) ).toBeTrue();
				expect( cls.init( "a.b", "1" ).equals( cls.init( "a.b", "1.0.0" ) ) ).toBeTrue();
				expect( cls.init( "a.b", "1.0.0" ).equals( cls.init( "a.b", "1.0.1" ) ) ).toBeFalse();
				expect( cls.init( "a.b", "1.0.0" ).equals( cls.init( "a.c", "1.0.0" ) ) ).toBeFalse();
				expect( cls.init( "a.b" ).equals( cls.init( "a.b", "1.0.0" ) ) ).toBeFalse();
				expect( cls.init( "a.b" ).equals( cls.init( "a.b" ) ) ).toBeTrue();
				expect( cls.init( "a.b", "1" ).hashCode() ).toBe( cls.init( "a.b", "1.0.0" ).hashCode() );
			});

		});
	}

	private function sgn( n ) {
		return n < 0 ? -1 : ( n > 0 ? 1 : 0 );
	}

	private function assertAscending( array chain, parser ) {
		var impl = createObject( "java", "lucee.loader.util.VersionImpl" );
		for ( var i = 1; i < arrayLen( chain ); i++ ) {
			for ( var j = i + 1; j <= arrayLen( chain ); j++ ) {
				expect( impl.compare( parser( chain[ i ] ), parser( chain[ j ] ) ) ).toBeLT( 0, "#chain[ i ]# < #chain[ j ]#" );
				expect( impl.compare( parser( chain[ j ] ), parser( chain[ i ] ) ) ).toBeGT( 0, "#chain[ j ]# > #chain[ i ]#" );
			}
		}
	}

}
