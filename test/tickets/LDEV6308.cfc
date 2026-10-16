component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	// ---- private query fixtures ----

	private query function getNumbers() {
		var qry = queryNew( "id,category,amount", "integer,varchar,integer", [
			[ 1, "alpha", 10 ],
			[ 2, "beta",  20 ],
			[ 3, "alpha", 30 ],
			[ 4, "gamma", 40 ],
			[ 5, "beta",  50 ]
		] );
		return qry;
	}

	private query function getOverlapA() {
		var qry = queryNew( "id,val", "integer,varchar", [
			[ 1, "a1" ],
			[ 2, "a2" ],
			[ 3, "a3" ]
		] );
		return qry;
	}

	private query function getOverlapB() {
		var qry = queryNew( "id,val", "integer,varchar", [
			[ 2, "a2" ],
			[ 3, "a3" ],
			[ 4, "b4" ]
		] );
		return qry;
	}

	private query function getMultiCol() {
		var qry = queryNew( "a,b", "varchar,varchar", [
			[ "x", "1" ],
			[ "x", "1" ],
			[ "x", "2" ],
			[ "y", "1" ]
		] );
		return qry;
	}

	private query function getSingleRow() {
		return queryNew( "id", "integer", [ [ 42 ] ] );
	}

	private query function getEmpty() {
		return queryNew( "id,amount", "integer,integer" );
	}

	// ---- helper: run on both engines and assert results match ----

	/**
	 * Runs the SQL via native QoQ AND HSQLDB engines, asserts cell-by-cell equivalence,
	 * returns the native result so individual specs can do additional assertions.
	 * Source queries are provided as a struct keyed by the name used in the SQL.
	 */
	private query function runBoth( required string sql, required struct sources, numeric maxrows = -1 ) {
		var nativeResult = runWithEngine( arguments.sql, arguments.sources, "native", arguments.maxrows );
		var hsqldbResult = runWithEngine( arguments.sql, arguments.sources, "hsqldb", arguments.maxrows );

		expect( nativeResult.recordcount ).toBe(
			hsqldbResult.recordcount,
			"recordcount mismatch: native=#nativeResult.recordcount#, hsqldb=#hsqldbResult.recordcount#"
		);

		var cols = listToArray( nativeResult.columnList );
		for ( var i = 1; i <= nativeResult.recordcount; i++ ) {
			for ( var col in cols ) {
				var nv = nativeResult[ col ][ i ];
				var hv = hsqldbResult[ col ][ i ];
				expect( nv ).toBe(
					hv,
					"cell mismatch at row #i# col '#col#': native=[#nv#], hsqldb=[#hv#]"
				);
			}
		}
		return nativeResult;
	}

	private query function runWithEngine( required string sql, required struct sources, required string engine, numeric maxrows = -1 ) {
		// Inject sources into local scope so queryExecute's QoQ source-lookup finds them
		for ( var name in arguments.sources ) {
			local[ name ] = arguments.sources[ name ];
		}
		var opts = { dbtype: { type: "query", engine: arguments.engine } };
		if ( arguments.maxrows > -1 ) opts.maxrows = arguments.maxrows;
		return queryExecute( arguments.sql, {}, opts );
	}

	function run( testResults, testBox ) {

		describe( title="LDEV-6308 - QoQ refactor coverage gaps (native vs hsqldb)", body=function() {

			// ---- ORDER BY ----

			it( title="ORDER BY two columns, mixed ASC/DESC", body=function( currentSpec ) {
				var r = runBoth(
					"SELECT id, category, amount FROM q ORDER BY category ASC, amount DESC",
					{ q: getNumbers() }
				);
				expect( r.recordcount ).toBe( 5 );
				// alpha group: 30 then 10 (DESC); beta: 50 then 20; gamma alone
				expect( r.category[ 1 ] ).toBe( "alpha" );
				expect( r.amount[ 1 ] ).toBe( 30 );
				expect( r.amount[ 2 ] ).toBe( 10 );
				expect( r.amount[ 3 ] ).toBe( 50 );
				expect( r.amount[ 4 ] ).toBe( 20 );
				expect( r.category[ 5 ] ).toBe( "gamma" );
			});

			it( title="ORDER BY DESC single column", body=function( currentSpec ) {
				var r = runBoth(
					"SELECT id FROM q ORDER BY id DESC",
					{ q: getNumbers() }
				);
				expect( r.recordcount ).toBe( 5 );
				expect( r.id[ 1 ] ).toBe( 5 );
				expect( r.id[ 5 ] ).toBe( 1 );
			});

			it( title="ORDER BY on single-row source", body=function( currentSpec ) {
				var r = runBoth(
					"SELECT id FROM q ORDER BY id",
					{ q: getSingleRow() }
				);
				expect( r.recordcount ).toBe( 1 );
				expect( r.id[ 1 ] ).toBe( 42 );
			});

			it( title="maxrows combined with ORDER BY DESC returns largest rows", body=function( currentSpec ) {
				// Verifies the limit is applied AFTER sorting, not before. If maxrows fired in
				// the per-row build loop it'd return [1,2] (source order); correct is [5,4].
				var r = runBoth(
					sql = "SELECT id FROM q ORDER BY id DESC",
					sources = { q: getNumbers() },
					maxrows = 2
				);
				expect( r.recordcount ).toBe( 2 );
				expect( r.id[ 1 ] ).toBe( 5 );
				expect( r.id[ 2 ] ).toBe( 4 );
			});

			// ---- UNION ----

			it( title="UNION DISTINCT removes duplicates across sources", body=function( currentSpec ) {
				var r = runBoth(
					"SELECT id, val FROM q1 UNION SELECT id, val FROM q2 ORDER BY id",
					{ q1: getOverlapA(), q2: getOverlapB() }
				);
				// {1,a1},{2,a2},{3,a3} ∪ {2,a2},{3,a3},{4,b4} = 4 distinct rows
				expect( r.recordcount ).toBe( 4 );
				expect( r.id[ 1 ] ).toBe( 1 );
				expect( r.id[ 4 ] ).toBe( 4 );
			});

			it( title="UNION ALL keeps duplicates across sources", body=function( currentSpec ) {
				var r = runBoth(
					"SELECT id, val FROM q1 UNION ALL SELECT id, val FROM q2 ORDER BY id",
					{ q1: getOverlapA(), q2: getOverlapB() }
				);
				expect( r.recordcount ).toBe( 6 );
			});

			// ---- GROUP BY / HAVING ----

			it( title="HAVING with column not in SELECT (additionalColumns path)", body=function( currentSpec ) {
				// `amount` is not in the SELECT list but referenced in HAVING — exercises
				// QueryPartitions' additionalColumns + additionalColRefs mechanism
				var r = runBoth(
					"SELECT category FROM q GROUP BY category HAVING SUM(amount) > 30 ORDER BY category",
					{ q: getNumbers() }
				);
				// alpha: 40, beta: 70, gamma: 40 — all > 30
				expect( r.recordcount ).toBe( 3 );
			});

			it( title="HAVING that filters all groups out", body=function( currentSpec ) {
				var r = runBoth(
					"SELECT category FROM q GROUP BY category HAVING SUM(amount) > 9999",
					{ q: getNumbers() }
				);
				expect( r.recordcount ).toBe( 0 );
			});

			it( title="GROUP BY combined with ORDER BY on aggregate", body=function( currentSpec ) {
				// Secondary sort on category disambiguates the tie between alpha (40) and gamma (40).
				// Without it, native and hsqldb break the tie in different orders — both correct
				// per SQL but they disagree, masking real bugs in the comparison harness.
				var r = runBoth(
					"SELECT category, SUM(amount) AS total FROM q GROUP BY category ORDER BY total DESC, category ASC",
					{ q: getNumbers() }
				);
				expect( r.recordcount ).toBe( 3 );
				// Sums: beta 70, alpha 40, gamma 40 — beta first, then alpha (ASC tie-break), then gamma
				expect( r.category[ 1 ] ).toBe( "beta" );
				expect( r.total[ 1 ] ).toBe( 70 );
				expect( r.category[ 2 ] ).toBe( "alpha" );
				expect( r.category[ 3 ] ).toBe( "gamma" );
			});

			// ---- Aggregates over empty ----

			it( title="Aggregate over empty WHERE result returns one row", body=function( currentSpec ) {
				var r = runBoth(
					"SELECT COUNT(*) AS cnt FROM q WHERE id > 100",
					{ q: getNumbers() }
				);
				expect( r.recordcount ).toBe( 1 );
				expect( r.cnt[ 1 ] ).toBe( 0 );
			});

			it( title="Aggregate over fully-empty source returns one row", body=function( currentSpec ) {
				var r = runBoth(
					"SELECT COUNT(*) AS cnt, SUM(amount) AS total FROM q",
					{ q: getEmpty() }
				);
				expect( r.recordcount ).toBe( 1 );
				expect( r.cnt[ 1 ] ).toBe( 0 );
			});

			// ---- DISTINCT ----

			it( title="DISTINCT on multi-column projection", body=function( currentSpec ) {
				var r = runBoth(
					"SELECT DISTINCT a, b FROM q ORDER BY a, b",
					{ q: getMultiCol() }
				);
				// {x,1},{x,1},{x,2},{y,1} → distinct: {x,1},{x,2},{y,1}
				expect( r.recordcount ).toBe( 3 );
			});
		});
	}
}
