component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll() {
		variables.nativeQoQ = { dbtype: { type: "query", engine: "native" } };
	}

	function run( testResults, testBox ) {

		describe( "LDEV-4300 QoQ HAVING without GROUP BY", function() {
			defineSpecs();

			it( title="HAVING without GROUP BY and without an aggregate in the select list is rejected", body=function() {
				var qry = queryNew( "col", "varchar", [ [ "a" ] ] );
				expect( function() {
					queryExecute( "SELECT col FROM qry HAVING col = 'a'", {}, variables.nativeQoQ );
				}).toThrow();
			});
		});

	}

	private function defineSpecs() {

		it( title="empty query, HAVING keeps the single aggregate row", body=function() {
			var qry = queryNew( "col", "varchar" );
			var result = queryExecute( "SELECT COUNT(1) AS cnt FROM qry HAVING COUNT(1) = 0", {}, variables.nativeQoQ );
			expect( result.recordCount ).toBe( 1 );
			expect( result.cnt ).toBe( 0 );
		});

		it( title="empty query, HAVING removes the single aggregate row", body=function() {
			var qry = queryNew( "col", "varchar" );
			var result = queryExecute( "SELECT COUNT(1) AS cnt FROM qry HAVING COUNT(1) > 0", {}, variables.nativeQoQ );
			expect( result.recordCount ).toBe( 0 );
		});

		it( title="non-empty query, HAVING keeps the single aggregate row", body=function() {
			var qry = queryNew( "col", "varchar", [ [ "a" ], [ "b" ], [ "c" ] ] );
			var result = queryExecute( "SELECT COUNT(1) AS cnt FROM qry HAVING COUNT(1) > 2", {}, variables.nativeQoQ );
			expect( result.recordCount ).toBe( 1 );
			expect( result.cnt ).toBe( 3 );
		});

		it( title="non-empty query, HAVING removes the single aggregate row", body=function() {
			var qry = queryNew( "col", "varchar", [ [ "a" ], [ "b" ], [ "c" ] ] );
			var result = queryExecute( "SELECT COUNT(1) AS cnt FROM qry HAVING COUNT(1) = 0", {}, variables.nativeQoQ );
			expect( result.recordCount ).toBe( 0 );
		});

		it( title="HAVING combined with WHERE", body=function() {
			var qry = queryNew( "num", "integer", [ [ 1 ], [ 2 ], [ 3 ], [ 4 ] ] );
			var kept = queryExecute( "SELECT COUNT(1) AS cnt FROM qry WHERE num > 1 HAVING COUNT(1) = 3", {}, variables.nativeQoQ );
			expect( kept.recordCount ).toBe( 1 );
			expect( kept.cnt ).toBe( 3 );
			var removed = queryExecute( "SELECT COUNT(1) AS cnt FROM qry WHERE num > 1 HAVING COUNT(1) = 4", {}, variables.nativeQoQ );
			expect( removed.recordCount ).toBe( 0 );
		});

		it( title="HAVING combined with a WHERE that matches no rows", body=function() {
			var qry = queryNew( "num", "integer", [ [ 1 ], [ 2 ] ] );
			var kept = queryExecute( "SELECT COUNT(1) AS cnt FROM qry WHERE num > 10 HAVING COUNT(1) = 0", {}, variables.nativeQoQ );
			expect( kept.recordCount ).toBe( 1 );
			expect( kept.cnt ).toBe( 0 );
			var removed = queryExecute( "SELECT COUNT(1) AS cnt FROM qry WHERE num > 10 HAVING COUNT(1) > 0", {}, variables.nativeQoQ );
			expect( removed.recordCount ).toBe( 0 );
		});

		it( title="HAVING with an aggregate on a column that is not selected", body=function() {
			var qry = queryNew( "num", "integer", [ [ 1 ], [ 2 ], [ 3 ] ] );
			var kept = queryExecute( "SELECT COUNT(1) AS cnt FROM qry HAVING SUM(num) = 6", {}, variables.nativeQoQ );
			expect( kept.recordCount ).toBe( 1 );
			expect( kept.cnt ).toBe( 3 );
			var removed = queryExecute( "SELECT COUNT(1) AS cnt FROM qry HAVING SUM(num) > 6", {}, variables.nativeQoQ );
			expect( removed.recordCount ).toBe( 0 );
		});

		it( title="HAVING on a selected aggregate with ORDER BY after it", body=function() {
			var qry = queryNew( "num", "integer", [ [ 1 ], [ 2 ], [ 3 ] ] );
			var result = queryExecute( "SELECT SUM(num) AS total FROM qry HAVING SUM(num) > 5 ORDER BY total", {}, variables.nativeQoQ );
			expect( result.recordCount ).toBe( 1 );
			expect( result.total ).toBe( 6 );
		});

		it( title="HAVING together with GROUP BY still works", body=function() {
			var qry = queryNew( "grp,num", "varchar,integer", [ [ "a", 1 ], [ "a", 2 ], [ "b", 5 ] ] );
			var result = queryExecute( "SELECT grp, SUM(num) AS total FROM qry GROUP BY grp HAVING SUM(num) > 3", {}, variables.nativeQoQ );
			expect( result.recordCount ).toBe( 1 );
			expect( result.grp ).toBe( "b" );
			expect( result.total ).toBe( 5 );
		});
	}
}
