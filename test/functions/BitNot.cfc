component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitNot()", body=function() {
			it(title="checking BitNot() function", body = function( currentSpec ) {
				assertEquals("-2",BitNot(1));
				assertEquals("-1",BitNot(0));
				assertEquals("-13",BitNot(12));
			});
		});
	}
}