component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi,maven" javaSettings='{
		"maven": [
			"org.mozilla:rhino:1.9.2"
		]
	}' {

	import "org.mozilla.javascript.Context";

	function run( testResults, testBox ) {
		describe( "LDEV-6791 jdk.* is part of the default bootdelegation", function() {

			it( title="jdk.* packages are in the effective bootdelegation", body=function( currentSpec ) {
				var osgiUtil = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
				expect( osgiUtil.isPackageInBootelegation( "jdk.dynalink" ) ).toBeTrue();
				expect( osgiUtil.isClassInBootelegation( "jdk.dynalink.RelinkableCallSite" ) ).toBeTrue();
			});

			it( title="a jdk.dynalink class is loadable from a Maven loaded context", body=function( currentSpec ) {
				var clazz = createObject( "java", "jdk.dynalink.RelinkableCallSite" );
				expect( clazz.getClass().getName() ).toBe( "jdk.dynalink.RelinkableCallSite" );
			});

			it( title="Rhino 1.9 compiles and runs a script (invokedynamic call sites)", body=function( currentSpec ) {
				var cx = Context::enter();
				try {
					var scope = cx.initStandardObjects();
					var result = cx.evaluateString( scope, "var o = { a: 40 }; o.a + 2;", "ldev6791", 1, nullValue() );
					expect( result ).toBe( 42 );
				}
				finally {
					Context::exit();
				}
			});

		});
	}

}
