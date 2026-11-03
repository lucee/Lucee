component extends="org.lucee.cfml.test.LuceeTestCase" labels="struct" {

	function run( testResults, testBox ) {
		describe( "LDEV-3423 structKeyTranslate() with nested structs / scopes", function() {

			it( "deep translate of the server scope does not throw", function() {
				var res = "";
				try {
					structKeyTranslate( server, true );
					res = "ok";
				}
				catch ( any e ) {
					res = e.message;
				}
				expect( res ).toBe( "ok" );
				// the JVM system properties must not be changed
				expect( structKeyExists( server.system.properties, "java.home" ) ).toBeTrue();
				expect( createObject( "java", "java.lang.System" ).getProperty( "java.home" ) ).notToBeEmpty();
			});

			it( "deep translate of a plain struct with a struct value", function() {
				var src = { "a.b": 1, "c": { "d.e": 2 } };
				structKeyTranslate( src, true );
				expect( src.a.b ).toBe( 1 );
				expect( src.c.d.e ).toBe( 2 );
			});

			it( "server.system.properties is translatable", function() {
				var s = {};
				structAppend( s, server.system.properties );
				structKeyTranslate( s, true );
				expect( s.java.home ).toBe( server.system.properties[ "java.home" ] );
			});
		});
	}
}
