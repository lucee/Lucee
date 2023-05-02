component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults , testBox ) {
		describe( title = "Testcase for queryColumnList", body = function() {
			var qry = queryNew("name,age");
			it(title = "checking queryColumnlist function", body = function( currentSpec ) {
				expect(queryColumnList(qry)).toBe("name,age");
			});

			it(title = "checking query.columnList member function", body = function( currentSpec ) {
				expect(qry.columnList()).toBe("name,age");
			});
		});
	}
}