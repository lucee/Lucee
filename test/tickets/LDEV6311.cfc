component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	// Locks the HSQLDB null-ordering contract: with the flag on (default),
	// HSQLDB matches native QoQ (nulls = low value -- first ASC, last DESC).
	// With the flag off, HSQLDB falls back to its engine default (nulls
	// always first regardless of direction). Both modes are exercised by
	// reading the runtime setting and flipping expectations to match.

	function beforeAll() {
		variables.nullsLastInDesc = isNullsLastInDescEnabled();
	}

	private boolean function isNullsLastInDescEnabled() {
		// Default true on 7.1+; only false if explicitly opted out via the sysprop/env var
		return getSystemPropOrEnvVar( "lucee.qoq.hsqldb.orderBy.nullsLastInDesc" ) != "false";
	}

	// Numeric fixture: amounts in source order [10, null, 30, null, 20]
	private query function getNumQry() {
		var q = queryNew( "id,amount", "integer,integer" );
		queryAddRow( q ); querySetCell( q, "id", 1 ); querySetCell( q, "amount", 10 );
		queryAddRow( q ); querySetCell( q, "id", 2 ); querySetCell( q, "amount", javaCast( "null", "" ) );
		queryAddRow( q ); querySetCell( q, "id", 3 ); querySetCell( q, "amount", 30 );
		queryAddRow( q ); querySetCell( q, "id", 4 ); querySetCell( q, "amount", javaCast( "null", "" ) );
		queryAddRow( q ); querySetCell( q, "id", 5 ); querySetCell( q, "amount", 20 );
		return q;
	}

	// Text fixture: vals ["beta", null, "alpha", null, "gamma"]
	private query function getTextQry() {
		var q = queryNew( "id,val", "integer,varchar" );
		queryAddRow( q ); querySetCell( q, "id", 1 ); querySetCell( q, "val", "beta" );
		queryAddRow( q ); querySetCell( q, "id", 2 ); querySetCell( q, "val", javaCast( "null", "" ) );
		queryAddRow( q ); querySetCell( q, "id", 3 ); querySetCell( q, "val", "alpha" );
		queryAddRow( q ); querySetCell( q, "id", 4 ); querySetCell( q, "val", javaCast( "null", "" ) );
		queryAddRow( q ); querySetCell( q, "id", 5 ); querySetCell( q, "val", "gamma" );
		return q;
	}

	// Mixed null + empty: ["beta", null, "", null, "alpha"]
	private query function getMixedQry() {
		var q = queryNew( "id,val", "integer,varchar" );
		queryAddRow( q ); querySetCell( q, "id", 1 ); querySetCell( q, "val", "beta" );
		queryAddRow( q ); querySetCell( q, "id", 2 ); querySetCell( q, "val", javaCast( "null", "" ) );
		queryAddRow( q ); querySetCell( q, "id", 3 ); querySetCell( q, "val", "" );
		queryAddRow( q ); querySetCell( q, "id", 4 ); querySetCell( q, "val", javaCast( "null", "" ) );
		queryAddRow( q ); querySetCell( q, "id", 5 ); querySetCell( q, "val", "alpha" );
		return q;
	}

	private string function runOrder( required string sql, required struct sources, required string engine ) {
		for ( var name in arguments.sources ) {
			local[ name ] = arguments.sources[ name ];
		}
		var r = queryExecute( arguments.sql, {}, { dbtype: { type: "query", engine: arguments.engine } } );
		return valueList( r.id );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6311 HSQLDB ORDER BY null position vs native", function() {

			it( "ASC matches between engines (always; flag-independent)", function() {
				var n = runOrder( "SELECT id FROM q ORDER BY amount ASC", { q: getNumQry() }, "native" );
				var h = runOrder( "SELECT id FROM q ORDER BY amount ASC", { q: getNumQry() }, "hsqldb" );
				expect( h ).toBe( n );
				expect( n ).toBe( "2,4,1,5,3" );
			});

			it( "numeric DESC: HSQLDB matches native iff flag enabled", function() {
				var n = runOrder( "SELECT id FROM q ORDER BY amount DESC", { q: getNumQry() }, "native" );
				var h = runOrder( "SELECT id FROM q ORDER BY amount DESC", { q: getNumQry() }, "hsqldb" );
				expect( n ).toBe( "3,5,1,2,4" );
				if ( variables.nullsLastInDesc ) {
					expect( h ).toBe( n, "engines should agree under default flag (true): native=[#n#] hsqldb=[#h#]" );
				}
				else {
					expect( h ).toBe( "2,4,3,5,1", "legacy HSQLDB: nulls first regardless of direction" );
				}
			});

			it( "text DESC: HSQLDB matches native iff flag enabled", function() {
				var n = runOrder( "SELECT id FROM q ORDER BY val DESC", { q: getTextQry() }, "native" );
				var h = runOrder( "SELECT id FROM q ORDER BY val DESC", { q: getTextQry() }, "hsqldb" );
				expect( n ).toBe( "5,1,3,2,4" );
				if ( variables.nullsLastInDesc ) {
					expect( h ).toBe( n, "engines should agree on text DESC under default flag" );
				}
				else {
					expect( h ).toBe( "2,4,5,1,3", "legacy HSQLDB nulls-first text DESC" );
				}
			});

			// Mixed null + "" cases: the fix aligns engines on null *position* but not
			// on null vs "" equivalence. Native conflates the two (Caster.toString(null)
			// returns "" inside TextComparator). HSQLDB strictly orders null before non-null
			// values including "". Both engines remain internally consistent; the lock
			// captures the new HSQLDB sequences so any future drift is caught.

			it( "mixed null+empty ASC: HSQLDB sequence locked", function() {
				var h = runOrder( "SELECT id FROM q ORDER BY val ASC", { q: getMixedQry() }, "hsqldb" );
				if ( variables.nullsLastInDesc ) {
					// nulls before "" before non-empty ASC
					expect( h ).toBe( "2,4,3,5,1" );
				}
				else {
					// legacy: same shape (HSQLDB always-first) -- ASC was unchanged
					expect( h ).toBe( "2,4,3,5,1" );
				}
				var n = runOrder( "SELECT id FROM q ORDER BY val ASC", { q: getMixedQry() }, "native" );
				expect( n ).toBe( "2,3,4,5,1", "native conflates null and empty string" );
			});

			it( "mixed null+empty DESC: HSQLDB sequence locked", function() {
				var h = runOrder( "SELECT id FROM q ORDER BY val DESC", { q: getMixedQry() }, "hsqldb" );
				if ( variables.nullsLastInDesc ) {
					// non-empty DESC, then "", then nulls
					expect( h ).toBe( "1,5,3,2,4" );
				}
				else {
					expect( h ).toBe( "2,4,1,5,3", "legacy HSQLDB nulls always first regardless" );
				}
				var n = runOrder( "SELECT id FROM q ORDER BY val DESC", { q: getMixedQry() }, "native" );
				expect( n ).toBe( "1,5,2,3,4", "native conflates null and empty string" );
			});

			it( "default dbtype routing produces native order on DESC under default flag", function() {
				// Reproducer from the ticket: dbtype="query" (no engine selector)
				// should agree with native + ACF, since HSQLDB now matches.
				var q = getNumQry();
				var r = queryExecute( "SELECT id FROM q ORDER BY amount DESC", {}, { dbtype: "query" } );
				if ( variables.nullsLastInDesc ) {
					expect( valueList( r.id ) ).toBe( "3,5,1,2,4",
						"default routing should produce native+ACF order under default flag" );
				}
				else {
					// Lucee may route to either engine; just assert the row count survives
					expect( r.recordcount ).toBe( 5 );
				}
			});
		});
	}
}
