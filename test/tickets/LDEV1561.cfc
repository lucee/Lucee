component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1561", function() {
			it( title='checking hash function with invalid iterations', body=function( currentSpec ) {
				var hs1 = hash("foo", "SHA-256", "UTF-8", -1);
				var hs2 = hash("foo", "SHA-256", "UTF-8",  0);
				var hs3 = hash("foo", "SHA-256", "UTF-8",  1);
				assertEquals("2C26B46B68FFC68FF99B453C1D30413413422D706483BFA0F98A5E886266E7AE", hs1);
				assertEquals("2C26B46B68FFC68FF99B453C1D30413413422D706483BFA0F98A5E886266E7AE", hs2);
				assertEquals("2C26B46B68FFC68FF99B453C1D30413413422D706483BFA0F98A5E886266E7AE", hs3);
			});
		});
	}
}