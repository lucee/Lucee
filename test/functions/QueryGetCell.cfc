component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults, testBox ) {
		describe( title = "Testcase for queryGetCell", body = function() {
			var myQuery = queryNew("id, name", "integer, varchar", [ [1, "Rajesh"], [2, "Anil"] ]);
			it(title = "checking queryGetCell function", body = function( currentSpec ) {
				expect(queryGetCell(myQuery, 'name', 1)).toBe("Rajesh");
				expect(queryGetCell(myQuery, 'id', 2)).toBe("2");
			});

			it(title = "checking query.getCell member function", body = function( currentSpec ) {
				expect(myQuery.getCell('name', 2)).toBe("Anil");
				expect(myQuery.getCell('id', 1)).toBe("1");
			});
		});
	}
}