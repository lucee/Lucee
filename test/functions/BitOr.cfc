component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitOr()", body=function() {
			it(title="Checking BitOr() function", body = function( currentSpec ) {
				assertEquals("1", BitOr(1, 0));
			});
		});
	}
}