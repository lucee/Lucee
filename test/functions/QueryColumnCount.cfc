component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults , testBox ) {
		describe( title = "Testcase for queryColumnCount", body = function() {
			var qry = queryNew("aaa,bbb,ccc,ddd,eee");;
			it(title = "checking queryColumnCount function", body = function( currentSpec ) {
				expect(queryColumnCount(qry)).toBe(5);
			});

			it(title = "checking query.columnCount member function", body = function( currentSpec ) {
				expect(qry.columnCount()).toBe(5);
			});
		});
	}
}