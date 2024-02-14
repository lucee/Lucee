component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults, textbox ) {
		describe( title = "Testcase for QueryEvery()", body = function() {
			var qry = queryNew("id, name", "cf_sql_integer, cf_sql_varchar", [ [ 1, "Tricia" ], [ 1, "Sarah" ], [ 1, "Joanna" ] ]);
			it(title = "Checking with QueryEvery()", body = function( currentSpec ) {
				var result = queryEvery(qry,function(row, age) {
					return (qry.id == 1);
				});
				expect(result).toBeTrue();
			});

			it(title = "Checking Query.every() member Function", body = function( currentSpec ) {
				var result = qry.every(function(row, age) {
					return (qry.name == "Sarah");
				});
				expect(result).toBeFalse();
			});
		});
	}
}