component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq"{

	function run( testResults , testBox ) {
		describe( title = "Test suite for LDEV-3514", body = function() {
			variables.q = queryNew("index, test, CompObj");
			queryAddRow(q);
			querySetCell(q, "index", 1);
			querySetCell(q, "test", "test");

			variables.q2 = queryNew("index, test");
			queryAddRow(q2);
			querySetCell(q2, "index", 1);
			querySetCell(q2, "test", "test");

			it(title = "Join QoQ containing CFC Object", skip=true, body = function( currentSpec ) {
				local.obj = new org.lucee.cfml.test.LuceeTestCase();
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

			it(title = "Native QoQ containing CFC Object", body = function( currentSpec ) {
				local.obj = new org.lucee.cfml.test.LuceeTestCase();
				querySetCell(q, "CompObj", obj);

				try {
					local.myQuery = queryExecute(
						"select q.index, q.test
						from q
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

			it(title = "Join QoQ containing a serialized CFC Object",  body = function( currentSpec ) {
				local.obj = new org.lucee.cfml.test.LuceeTestCase();
				querySetCell(q, "CompObj", serialize(obj));

				try {
					local.myQuery = queryExecute(
						"select * 
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
				expect(res).toBe("test");
			});

			it(title = "Native QoQ containing a serialized CFC Object",  body = function( currentSpec ) {
				local.obj = new org.lucee.cfml.test.LuceeTestCase();
				querySetCell(q, "CompObj", serialize(obj));
				try {
					local.myQuery = queryExecute(
						"select *
						from q
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