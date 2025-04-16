component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1339", function() {
			it(title="deserializeJSON with scientific notation", body = function( currentSpec ) {
				var res=deserializeJSON('{ "relevance": 2E-7 }');
				expect(res.relevance).toBe(0.0000002);

				res=deserializeJSON('{ "relevance": 2e-07 }');
				expect(res.relevance).toBe(0.0000002);
			});

			it(title="evaluate with scientific notation", body = function( currentSpec ) {
				var res=evaluate('{ "relevance": 2E-7 }');
				expect(res.relevance).toBe(0.0000002);
				res=evaluate('{ "relevance": 2e-07 }');
				expect(res.relevance).toBe(0.0000002);
			});

		});
	}

}