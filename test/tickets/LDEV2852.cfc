component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2852", function() {
			it( title = "Works fine with Non-member function", body = function( currentSpec ) {
				assertEquals(5,FindOneOf("o", "findoneof"));
				assertEquals(9,FindOneOf("f", "findoneof", 2));
				assertEquals(3,FindOneOf("lbw","aablew"));
			});

			it( title = "Works not correct with Member function", body = function( currentSpec ) {
				assertEquals(9,"findoneof".FindOneOf("f", 2));
				assertEquals(8,"findoneof".FindOneOf("o", 6));
				assertEquals(3,"aablew".FindOneOf("lbw"));
			});
		});
	}
}