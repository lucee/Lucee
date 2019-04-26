component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitXOr()", body=function() {
			it(title="Checking BitXOr() function", body = function( currentSpec ) {
				assertEquals("2",BitXOr(1, 3));
			});
		});
	}
}