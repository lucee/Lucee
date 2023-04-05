component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4229", function() {
			variables.q = queryNew("id,name", "Integer,varchar", [[1,"test"]]);
			it( title="Checking query param missing exception with positional param", body=function( currentSpec ) {
				try {
					var sql = "SELECT * FROM q WHERE name = ?";
					queryExecute(sql, [], {dbtype="query"});
				}
				catch(any e) {
					var result = e.detail;
				}
				expect(result).toInclude(sql);
			});

			it( title="Checking query param missing exception with named param", body=function( currentSpec ) {
				try {
					var sql = "SELECT * FROM q WHERE name = :id";
					queryExecute(sql, [] ,{dbtype="query"});
				}
				catch(any e) {
					var result = e.detail;
				}
				expect(result).toInclude(sql);
			});
		});
	}
}