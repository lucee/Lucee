component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1553", function() {
			it( title='checking createTimeSpan() return type as boolean', body=function( currentSpec ) {
				var ts = createTimeSpan(0, 0, 0, 1);
				assertEquals(true, isBoolean(ts));
			});
		});
	}
}