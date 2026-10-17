component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function run( testResults, testBox ) {
		describe( title="Test suite for jfrAvailable()", body=function() {
			it( title="checking jfrAvailable() function", body=function( currentSpec ) {
				var available = jfrAvailable();
				expect( available ).toBeTypeOf( "boolean" );
			} );
		} );
	}
}
