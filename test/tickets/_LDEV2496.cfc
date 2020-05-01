component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title = "Test suite for LDEV2496", body = function() {
			it( title = 'Test case for inputBaseN() of base 10',body = function( currentSpec ) {
				assertEquals('8999999999999999',inputBaseN("8999999999999999",10));
				assertEquals('8999999999999998',inputBaseN("8999999999999998",10));
				assertEquals('9999999999999999',inputBaseN("9999999999999999",10));
				assertEquals('9999999999999998',inputBaseN("9999999999999998",10));
			});

			it( title = 'Test case for inputBaseN() of base 2',body = function( currentSpec ) {
				assertEquals('536349441 ',inputBaseN("11111111110000000101100000001",2));
				assertEquals('536349441 ',inputBaseN(11111111110000000101100000001,2));
			});
		});
	}
}