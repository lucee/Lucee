component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1827", function() {
			it( title='checking final variable', body=function( currentSpec ) {
				var obj = new LDEV1827.test();
				assertEquals("foo", obj.bar());
			});
		});
	}
}