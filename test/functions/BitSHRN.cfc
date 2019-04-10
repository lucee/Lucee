component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitSHRN()", body=function() {
			it(title="Checking BitSHRN() function", body = function( currentSpec ) {
				assertEquals("0",BitSHRN(1,1));
				assertEquals("0",BitSHRN(1,30));
				assertEquals("0",BitSHRN(1,31));
				assertEquals("0",BitSHRN(2,31));
				assertEquals("32",BitSHRN(128,2));
			});
		});
	}
}
