component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2701", function() {
			it(title = "precision issue", body = function( currentSpec ) {
				expect(294.02*100).toBe(29402);
				expect(294.06*100).toBe(29406);
				expect(294.09*100).toBe(29406);
				expect(294.09*10).toBe(2940.9);
				expect(294.01*100).toBe(29401);
				expect(294.03*100).toBe(29403);
				expect(294.04*100).toBe(29404);
			});
		});
	}
}
