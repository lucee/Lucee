component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1778", function() {
			it( title="getDynamicProxy() should work with core JDK interfaces", body=function() {
				var cfcInstance   = new LDEV1778.Runnable();
				var proxiedObject = "";
				var out           = "";

				proxiedObject = CreateDynamicProxy( cfcInstance, "java.lang.Runnable" );
				saveContent variable="out" {
					proxiedObject.run();
				}
				expect( out ).toBe( "test" );
			});
		});
	}
}
