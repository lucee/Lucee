component extends="org.lucee.cfml.test.LuceeTestCase" labels="query"{

	function run( testResults , testBox ) {
		describe( title = "Testcase for querySetCell", body = function() {
			var qry = queryNew( "id, name");
			queryAddRow(qry);
			it(title = "checking querySetCell function", body = function( currentSpec ) {
				querySetCell(qry, "id", 1, 1);
				expect(qry.id).toBe(1);
			});

			it(title = "checking query.setCell member function", body = function( currentSpec ) {
				qry.setCell("name", "one", 1);
				expect(qry.name).toBe("one");
			});
		});
	}
}