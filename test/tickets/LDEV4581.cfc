component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults , testBox ) {
		describe( title = "Testcase for query.columnList.listToArray() function", body = function() {
			var qry =  queryNew("age,name", "integer,varchar", []);
			it( title = "checking query.columnList.listToArray() function", body = function( currentSpec ) {
				expect(qry.columnList.listToArray()[2]).toBe("name");
			});
		});
	}
}