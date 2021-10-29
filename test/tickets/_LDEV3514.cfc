component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title = "Test suite for LDEV-3514", body = function() {
			variables.q2 = queryNew("index, test");
				queryAddRow(q2);
				querySetCell(q2, "index", 1);
				querySetCell(q2, "test", "test2");

			it(title = "Query containing CFC Object without serialize", body = function( currentSpec ) {
				local.obj = new test();
				local.q = queryNew("index, test, CompObj");
				queryAddRow(q);
				querySetCell(q, "index", 1);
				querySetCell(q, "test", "test");
				querySetCell(q, "CompObj", obj);

				try {
					local.myQuery = queryExecute(
					"select q.index, q.test
					from q, q2
					where q.index = q2.index
					order by q.index", 
					{}, 
					{dbtype = "query"} 
					);
					local.res = myquery.test
				} catch (any e) {
					res = e.message
				}
				expect(res).toBe("test2");
			}); 
			
			it(title = "Query containing CFC Object with serialize object", body = function( currentSpec ) {
				local.obj = new test();
				local.q = queryNew("index, test, CompObj");
				queryAddRow(q);
				querySetCell(q, "index", 1);
				querySetCell(q, "test", "test");
				querySetCell(q, "CompObj", serialize(obj));

				try {
					
					local.myQuery = queryExecute(
					"select * from q, q2
					where q.index = q2.index
					order by q.index", 
					{}, 
					{dbtype = "query"} 
					);
					local.res = myquery.test
				} catch (any e) {
					res = e.message
				}
				expect(res).toBe("test");
			});
		});
	}
}