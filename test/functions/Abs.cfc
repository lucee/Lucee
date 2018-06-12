component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for abs()", body=function() {
			it(title="checking abs() function", body = function( currentSpec ) {
				assertEquals(1,abs(1));
				assertEquals(1.9,abs(1.9));
				assertEquals(1.9,abs(-1.9));
				assertEquals(1.9,abs(+1.9));
				assertEquals(0,abs(0));
				assertEquals(0,abs(-0));
				assertEquals(0,abs("0"));
			});
		});
	}
}