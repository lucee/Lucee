component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults, textbox ) {
		describe( title = "Testcase for QuerySlice()", body = function() {
			var qry = queryNew("id, name", "cf_sql_integer, cf_sql_varchar", [ [ 1, "Tricia" ], [ 2, "Sarah" ], [ 3, "Joanna" ] ]);
			it(title = "Checking with QuerySlice()", body = function( currentSpec ) {
				var result = querySlice(qry, 2, 2);
				expect(result.recordCount()).toBe(2);
			});

			it(title = "Checking Query.slice() with memberFunction", body = function( currentSpec ) {
				var result = qry.slice(3,1);
				expect(result.recordCount()).toBe(1);
			});
		});
	}
}