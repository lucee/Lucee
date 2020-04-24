component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV2654", function() {
			it( title = "Int function", body = function( currentSpec ) {
				
				expect(Int( 720.11111111111111111111111111111111 )).toBe(720);
				expect(Int( 720.075 )).toBe(720);
				expect(Int( 720.0000000000000000000000252 )).toBe(720);
				expect(Int( 720.00000000000000000000000000000252 )).toBe(720);
			});

			it( title = "Floor function", body = function( currentSpec ) {
				
				expect(Floor( 720.11111111111111111111111111111111 )).toBe(720);
				expect(Floor( 720.075 )).toBe(720);
				expect(Floor( 720.0000000000000000000000252 )).toBe(720);
				expect(Floor( 720.00000000000000000000000000000252 )).toBe(720);
			});

		});
	}

}