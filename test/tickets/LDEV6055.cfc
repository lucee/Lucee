component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6055 - super.method() error message", function() {

			it( "should NOT say 'private function' when calling non-existent method via super", function() {
				var child = new LDEV6055.Child();
				try {
					child.callSuperNonExistent();
					fail( "Should have thrown an error" );
				}
				catch ( any e ) {
					// The error should NOT mention "private" since we're calling via super
					// and the access level issue is misleading
					expect( e.message ).notToInclude( "private" );
				}
			} );

		} );
	}

}
