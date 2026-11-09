component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6313 bundle not available error keeps the underlying download error as cause", function() {

			// TODO LDEV-6627: without OSGi nothing is downloaded, so there is no cause, skipped as long as OSGi is not installed.
			it( title="the BundleException has the failed download as its cause", skip=!server._hasOSGi(), body=function() {
				var caught = false;
				try {
					createObject( "java", "org.ldev6313.DoesNotExist", "org.lucee.ldev6313.does-not-exist", "1.2.3" );
				}
				catch ( any e ) {
					caught = true;
					expect( e.message ).toInclude( "org.lucee.ldev6313.does-not-exist" );
					expect( structKeyExists( e, "cause" ) && !isNull( e.cause ) ).toBeTrue( "missing cause" );
					expect( len( e.cause.message ?: "" ) ).toBeGT( 0 );
				}
				expect( caught ).toBeTrue( "createObject should have thrown" );
			});
		});
	}
}
