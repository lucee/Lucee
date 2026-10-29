component extends="org.lucee.cfml.test.LuceeTestCase" labels="com" {

	function run( testResults, testBox ) {
		describe( "LDEV-6763 COM support moved to the COM extension", function() {

			it( 'createObject("com") is not supported anymore and points to createComObject', function() {
				try {
					createObject( "com", "Scripting.FileSystemObject" );
					fail( "createObject(""com"") must throw an exception" );
				}
				catch ( any e ) {
					expect( e.message ).toInclude( "no longer part of the Lucee core" );
					expect( e.detail ).toInclude( "createComObject" );
				}
			});

			it( 'cfobject type="com" is not supported anymore and points to createComObject', function() {
				try {
					savecontent variable="local.out" {
						include "LDEV6763/cfobject.cfm";
					}
					fail( "cfobject type=com must throw an exception" );
				}
				catch ( any e ) {
					expect( e.message ).toInclude( "no longer part of the Lucee core" );
					expect( e.detail ).toInclude( "createComObject" );
				}
			});

			it( "the Jacob library is not part of the core anymore", function() {
				expect( function() {
					createObject( "java", "lucee.commons.jacob.com.Dispatch" );
				}).toThrow();
				expect( function() {
					createObject( "java", "lucee.runtime.com.COMObject" );
				}).toThrow();
			});

		});
	}
}
