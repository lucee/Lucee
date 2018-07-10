component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1778", function() {
			it( title="getDynamicProxy() should work with core JDK interfaces", body=function() {
				var cfcInstance   = new LDEV1778.Runnable();
				var proxiedObject = "";
				var out           = "";

				try {
					proxiedObject = CreateDynamicProxy( cfcInstance, "java.lang.Runnable" );
				} catch( "java.lang.NoClassDefFoundError" e ) {
					fail( "CreateDynamicProxy raised a java.lang.NoClassDefFoundError error for core JVM interface 'java.lang.Runnable'" );
				}

				saveContent variable="out" {
					proxiedObject.run();
				}
				expect( out ).toBe( "test" );
			});
		});
	}
}