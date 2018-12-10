component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitAnd()", body=function() {
			it(title="Checking BitAnd() function", body = function( currentSpec ) {
				assertEquals("0",BitAnd(1, 0));
				assertEquals("0",BitAnd(0, 0));
				assertEquals("0",BitAnd(1, 2));
				assertEquals("1",BitAnd(1, 3));
				assertEquals("1",BitAnd(3, 5));
				assertEquals("1",BitAnd(1, 1.0));
				assertEquals("1",BitAnd(1, 1.1));
				assertEquals("1",BitAnd(1, 1.9));
				assertEquals("0",BitAnd(1, 0.9999999));
			});
		});
	}
}