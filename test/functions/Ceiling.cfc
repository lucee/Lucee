component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for Ceiling()", body=function() {
			it(title="checking Ceiling() function", body = function( currentSpec ) {
				assertEquals("4","#ceiling(3.4)#");
				assertEquals("3","#ceiling(3)#");
				assertEquals("4","#ceiling(3.8)#");
				assertEquals("4","#ceiling(3.4)#");
			});
		});
	}
}
