component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq"{

	private query function createQ( obj ) {
		var q = queryNew( "index,test,CompObj" );
		queryAddRow( q );
		querySetCell( q, "index", 1 );
		querySetCell( q, "test", "test" );
		querySetCell( q, "CompObj", arguments.obj );
		queryAddRow( q );
		querySetCell( q, "index", 2 );
		querySetCell( q, "test", "other" );
		querySetCell( q, "CompObj", "simple" );
		return q;
	}

	private query function createQ2() {
		var q2 = queryNew( "index,test" );
		queryAddRow( q2 );
		querySetCell( q2, "index", 1 );
		querySetCell( q2, "test", "test2" );
		return q2;
	}

	function run( testResults , testBox ) {
		describe( "LDEV-3514 join QoQ (HSQLDB) with a complex value in a column", function() {

			it( "join, the CFC column is not selected", function() {
				var q = createQ( new org.lucee.cfml.test.LuceeTestCase() );
				var q2 = createQ2();
				var r = queryExecute( "select q.index, q.test, q2.test as test2 from q, q2 where q.index = q2.index", {}, { dbtype: "query" } );
				expect( r.recordcount ).toBe( 1 );
				expect( r.test ).toBe( "test" );
				expect( r.test2 ).toBe( "test2" );
			});

			it( "join, the CFC column is selected and comes back as the component", function() {
				var obj = new org.lucee.cfml.test.LuceeTestCase();
				var q = createQ( obj );
				var q2 = createQ2();
				var r = queryExecute( "select q.index, q.CompObj from q, q2 where q.index = q2.index", {}, { dbtype: "query" } );
				expect( r.recordcount ).toBe( 1 );
				expect( isObject( r.CompObj[ 1 ] ) ).toBeTrue();
				expect( r.CompObj[ 1 ] ).toBe( obj );
			});

			it( "join, struct and array values and simple values in the same column", function() {
				var q = queryNew( "index,val" );
				queryAddRow( q, 3 );
				querySetCell( q, "index", 1, 1 ); querySetCell( q, "val", { a: 1 }, 1 );
				querySetCell( q, "index", 2, 2 ); querySetCell( q, "val", [ 1, 2 ], 2 );
				querySetCell( q, "index", 3, 3 ); querySetCell( q, "val", "plain", 3 );
				var q2 = queryNew( "index", "integer", [ [ 1 ], [ 2 ], [ 3 ] ] );
				var r = queryExecute( "select q.index, q.val from q, q2 where q.index = q2.index order by q.index", {}, { dbtype: "query" } );
				expect( r.recordcount ).toBe( 3 );
				expect( isStruct( r.val[ 1 ] ) ).toBeTrue();
				expect( r.val[ 1 ].a ).toBe( 1 );
				expect( isArray( r.val[ 2 ] ) ).toBeTrue();
				expect( r.val[ 3 ] ).toBe( "plain" );
			});
		});

		describe( title = "Test suite for LDEV-3514", body = function() {
			variables.q = queryNew("index, test, CompObj");
			queryAddRow(q);
			querySetCell(q, "index", 1);
			querySetCell(q, "test", "test");

			variables.q2 = queryNew("index, test");
			queryAddRow(q2);
			querySetCell(q2, "index", 1);
			querySetCell(q2, "test", "test");

			it(title = "Join QoQ containing CFC Object", body = function( currentSpec ) {
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
				expect(res).toBe("test");
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