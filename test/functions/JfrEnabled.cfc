component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function run( testResults, testBox ) {
		describe( title="Test suite for jfrEnabled()", body=function() {
			it( title="checking jfrEnabled() function", body=function( currentSpec ) {
				var enabled = jfrEnabled();
				expect( enabled ).toBeTypeOf( "boolean" );
			} );
		} );
	}
}
