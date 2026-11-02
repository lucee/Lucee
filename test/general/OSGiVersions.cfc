component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	function run( testResults, testBox ) {
		describe( "versions of bundles with a qualifier that is not a number", function() {

			it( "VersionImpl.parseOSGi accepts the qualifiers OSGi accepts", function() {
				var impl = createObject( "java", "lucee.loader.util.VersionImpl" );
				for ( var v in [ "12.2.0.jre8", "5.2.0.RELEASE", "2.0.0.Final", "1.0.0.v20200101", "1.0.0.SNAPSHOT", "1.0.0.20240101123456", "9.0.0.5-ALPHA", "1.2.3", "1.2" ] ) {
					expect( isNull( impl.parseOSGi( v, javaCast( "null", "" ) ) ) ).toBeFalse( "[#v#] should be a valid version" );
				}
			});

			it( "VersionImpl.parseOSGi rejects what is not a version", function() {
				var impl = createObject( "java", "lucee.loader.util.VersionImpl" );
				for ( var v in [ "abc", "", "1.a.0", "1.0.0.a b", "1.0.0.1.2" ] ) {
					expect( isNull( impl.parseOSGi( v, javaCast( "null", "" ) ) ) ).toBeTrue( "[#v#] should not be a valid version" );
				}
			});

			it( "a class definition keeps the bundle version 12.2.0.jre8", function() {
				var cd = createObject( "java", "lucee.transformer.library.ClassDefinitionImpl" ).init( "org.lucee.Dummy", "org.lucee.mssql", "12.2.0.jre8", javaCast( "null", "" ) );
				expect( cd.getVersionAsString() ).toBe( "12.2.0.jre8" );
				expect( isNull( cd.getVersion() ) ).toBeFalse();
			});

			it( "orders versions with a non numeric qualifier", function() {
				var impl = createObject( "java", "lucee.loader.util.VersionImpl" );
				var a = impl.parseOSGi( "12.2.0.jre8", javaCast( "null", "" ) );
				var b = impl.parseOSGi( "12.3.0.jre8", javaCast( "null", "" ) );
				expect( impl.compare( a, b ) ).toBeLT( 0 );
				expect( impl.compare( b, a ) ).toBeGT( 0 );
			});

		});
	}

}
