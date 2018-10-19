component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitSHLN()", body=function() {
			it(title="Checking BitSHLN() function", body = function( currentSpec ) {
				assertEquals("2", BitSHLN(1,1));
				assertEquals("1073741824", BitSHLN(1,30));
				assertEquals("-2147483648", BitSHLN(1,31));
				assertEquals("0", BitSHLN(2,31));
			});
		});
	}
}