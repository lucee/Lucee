component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" skip="true" {
	function run( testResults , testBox ) {
		describe( "testcase for LDEV-4300", function() {
			it(title = "Checking QoQ HAVING clause in non-grouped aggregate select", body = function( currentSpec ) {
				try {
					var qry = queryNew('col', 'varchar');
					var result = queryExecute(
						"SELECT COUNT(1) FROM qry HAVING COUNT(1) = 0",
						{},
						{ dbtype : "query" }
					).recordCount;
				} 
				catch (any e) {
					var result = e.message;
				}
				expect(result).toBe("1");
			});
		});
	}
}
