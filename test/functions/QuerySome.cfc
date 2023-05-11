component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults, textbox ) {
		describe( title = "Testcase for QuerySome()", body = function() {
			var qry = queryNew("id, name", "cf_sql_integer, cf_sql_varchar", [ [ 1, "Tricia" ], [ 2, "Sarah" ], [ 3, "Joanna" ] ]);
			it(title = "Checking with QuerySome()", body = function( currentSpec ) {
				var result = querySome(qry,function(row, age) {
					return (qry.id == 1);
				});
				expect(result).toBeTrue();
			});

			it(title = "Checking Query.some() with member Function", body = function( currentSpec ) {
				var result = qry.Some(function(row, age){
					return (qry.name == "Sarah");
				});
				expect(result).toBeTrue();
			});
		});
	}
}