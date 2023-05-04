component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults, testBox ) {
		describe( title = "Testcase for queryCurrentRow", body = function() {
			var myQuery = queryNew("id, name", "integer, varchar", [ [1, "Rajesh"]]);
			it(title = "checking queryCurrentRow function", body = function( currentSpec ) {
				expect(queryCurrentRow(myQuery)).toBe("1");
			});

			it(title = "checking query.currentRow member function", body = function( currentSpec ) {
				expect(myQuery.currentRow()).toBe("1");
			});
		});
	}
}