component extends="org.lucee.cfml.test.LuceeTestCase" labels="query,cache" {

	// LDEV-1406: a query returned from the query cache is shared with the cache entry and copied on the first
	// write (copy on write). Behaviour must be identical to a full copy: no change to a returned query may ever
	// show up in the cache entry or in the next query coming from the cache.

	function beforeAll() {
		variables.src = queryNew( "id,name,amount", "integer,varchar,double", [ [ 1, "a", 1.5 ], [ 2, "b", 2.5 ], [ 3, "c", 3.5 ] ] );
		variables.srcWithDate = queryNew( "id,name,day", "integer,varchar,timestamp", [ [ 1, "a", createDateTime( 2020, 1, 1, 1, 1, 1 ) ], [ 2, "b", createDateTime( 2020, 2, 2, 2, 2, 2 ) ] ] );
		variables.srcWithObject = queryNew( "id,name,obj", "integer,varchar,object", [ [ 1, "a", { a: 1 } ], [ 2, "b", [ 1, 2 ] ] ] );
		variables.counter = 0;

		// the specs need a default query cache that decouples (copies) the entries, CI might have none or another one
		try {
			variables.prevQueryCache = cacheGetDefaultCacheName( "query" );
		}
		catch ( any e ) {
			variables.prevQueryCache = "";
		}
		var caches = {};
		caches[ "ldev1406QueryCache" ] = {
			class: "lucee.runtime.cache.ram.RamCache",
			storage: false,
			default: "query",
			custom: { "timeToIdleSeconds": 3600, "timeToLiveSeconds": 3600 }
		};
		application action="update" caches=caches;
	}

	function afterAll() {
		try {
			cacheClear( cacheName="ldev1406QueryCache" );
		}
		catch ( any e ) {}
		if ( len( variables.prevQueryCache ) ) {
			application action="update" caches={ "query": variables.prevQueryCache };
		}
	}

	function run( testResults, testBox ) {

		describe( "LDEV-1406 copy on write for cached queries", function() {

			// mutators: every function receives a query that came from the cache and changes it
			var mutators = {
				"querySetCell": function( q ) { querySetCell( q, "name", "X", 2 ); },
				"querySetCell on the last row": function( q ) { querySetCell( q, "amount", 99, 3 ); },
				"column array assignment": function( q ) { q.name[ 2 ] = "X"; },
				"column array assignment, number": function( q ) { q.amount[ 1 ] = 99; },
				"column compound assignment": function( q ) { q.amount[ 1 ] += 100; },
				"cell assignment via query scope": function( q ) { q[ "name" ][ 3 ] = "X"; },
				"queryAddRow": function( q ) { queryAddRow( q, { id: 9, name: "z", amount: 9 } ); },
				"queryAddRow with empty row": function( q ) { queryAddRow( q ); },
				"member addRow": function( q ) { q.addRow( { id: 9, name: "z", amount: 9 } ); },
				"member addRow count": function( q ) { q.addRow( 3 ); },
				"queryDeleteRow": function( q ) { queryDeleteRow( q, 1 ); },
				"member deleteRow": function( q ) { q.deleteRow( 3 ); },
				"queryAddColumn": function( q ) { queryAddColumn( q, "extra", [ 1, 2, 3 ] ); },
				"member addColumn": function( q ) { q.addColumn( "extra", [ 1, 2, 3 ] ); },
				"queryDeleteColumn": function( q ) { queryDeleteColumn( q, "amount" ); },
				"member deleteColumn": function( q ) { q.deleteColumn( "name" ); },
				"querySort": function( q ) { querySort( q, "name", "desc" ); },
				"member sort": function( q ) { q.sort( "amount", "desc" ); },
				"member sort with closure": function( q ) { q.sort( function( a, b ) { return compare( b.name, a.name ); } ); },
				"querySetRow": function( q ) { querySetRow( q, 2, { id: 7, name: "Y", amount: 7 } ); },
				"queryAppend": function( q ) { queryAppend( q, queryNew( "id,name,amount", "integer,varchar,double", [ [ 8, "h", 8 ] ] ) ); },
				"queryPrepend": function( q ) { queryPrepend( q, queryNew( "id,name,amount", "integer,varchar,double", [ [ 8, "h", 8 ] ] ) ); },
				"queryInsertAt": function( q ) { queryInsertAt( q, { id: 5, name: "ins", amount: 5 }, 2 ); },
				"queryRowSwap": function( q ) { queryRowSwap( q, 1, 3 ); },
				"queryRenameColumn": function( q ) { queryRenameColumn( q, "name", "renamed" ); },
				"queryClear": function( q ) { queryClear( q ); },
				"queryConvertTypes": function( q ) { queryConvertTypes( q ); },
				"java setAt": function( q ) { q.setAt( "name", 2, "X" ); },
				"java setAtEL": function( q ) { q.setAtEL( "name", 2, "X" ); },
				"java addRow": function( q ) { q.addRow( 2 ); },
				"java removeRow": function( q ) { q.removeRow( 1 ); },
				"java removeRowEL": function( q ) { q.removeRowEL( 1 ); },
				"java removeColumn": function( q ) { q.removeColumn( "name" ); },
				"java removeColumnEL": function( q ) { q.removeColumnEL( "name" ); },
				"column add via java": function( q ) { q.getColumn( "name" ).add( "X" ); },
				"column set via java": function( q ) { q.getColumn( "name" ).set( 1, "X" ); },
				"column removeRow via java": function( q ) { q.getColumn( "name" ).removeRow( 1 ); },
				"column clear via java": function( q ) { q.getColumn( "name" ).clear(); },
				"queryColumnData, then change the returned array": function( q ) { var a = queryColumnData( q, "name" ); a[ 1 ] = "X"; arrayAppend( a, "Y" ); },
				"queryRowData, then change the returned struct": function( q ) { var s = queryRowData( q, 1 ); s.name = "X"; },
				"column loop assignment": function( q ) { loop query="q" { q.amount[ q.currentRow ] = q.amount * 10; } }
			};

			for ( var title in mutators ) {
				var fn = mutators[ title ];
				// closures capture loop variables by reference, hence the helper
				addMutatorSpec( title, fn );
			}

			it( title="a read of the cached query does not copy the data", body=function() {
				var sql = uniqueSql( "reads" );
				var q1 = cachedQuery( sql ); // miss, goes into the cache
				var before = snapshot( q1 );
				var q2 = cachedQuery( sql ); // hit
				expect( cachedFlag( sql ) ).toBeTrue();
				// reading in all possible ways
				var x = q2.recordCount & q2.columnList;
				loop query="q2" { x &= q2.id & q2.name & q2.amount; }
				x &= q2.name[ 2 ] & q2.amount[ 3 ];
				x &= serializeJSON( q2 );
				x &= queryColumnData( q2, "name" ).len();
				x &= structCount( queryRowData( q2, 2 ) );
				q2.each( function( row ) {} );
				x &= q2.filter( function( row ) { return row.id > 1; } ).recordCount;
				x &= q2.map( function( row ) { return row; } ).recordCount;
				x &= duplicate( q2 ).recordCount; // duplicate is a real copy, it must not touch the sharing
				var cols = q2.getColumnNames();
				for ( var c in cols ) {
					expect( q2.getColumn( c ).isDataShared() ).toBeTrue( "column [#c#] was copied by a read" );
				}
				expect( snapshot( q2 ) ).toBe( before );
			} );

			it( title="only the written column gets copied, the others stay shared", body=function() {
				var sql = uniqueSql( "onlycol" );
				cachedQuery( sql );
				var q = cachedQuery( sql );
				expect( q.getColumn( "name" ).isDataShared() ).toBeTrue();
				expect( q.getColumn( "amount" ).isDataShared() ).toBeTrue();
				q.name[ 2 ] = "X";
				expect( q.getColumn( "name" ).isDataShared() ).toBeFalse( "written column must be private" );
				expect( q.getColumn( "id" ).isDataShared() ).toBeTrue( "untouched column must still be shared" );
				expect( q.getColumn( "amount" ).isDataShared() ).toBeTrue( "untouched column must still be shared" );
				expect( q.name[ 2 ] ).toBe( "X" );
				expect( q.name[ 1 ] ).toBe( "a" );
				// second write is done in place
				q.name[ 3 ] = "Y";
				expect( q.name[ 3 ] ).toBe( "Y" );
				// the cache is untouched
				var q2 = cachedQuery( sql );
				expect( q2.name[ 2 ] ).toBe( "b" );
				expect( q2.name[ 3 ] ).toBe( "c" );
			} );

			it( title="a query that is a copy (duplicate) is never shared", body=function() {
				var sql = uniqueSql( "dup" );
				cachedQuery( sql );
				var q = cachedQuery( sql );
				var d = duplicate( q );
				expect( d.getColumn( "name" ).isDataShared() ).toBeFalse();
				d.name[ 1 ] = "X";
				expect( q.name[ 1 ] ).toBe( "a" );
				expect( cachedQuery( sql ).name[ 1 ] ).toBe( "a" );
			} );

			it( title="the query that was put into the cache can be changed without changing the cache", body=function() {
				var sql = uniqueSql( "first" );
				var q1 = cachedQuery( sql ); // the first one is the one that was stored
				var before = snapshot( q1 );
				q1.name[ 1 ] = "X";
				querySetCell( q1, "amount", 77, 2 );
				queryAddRow( q1, { id: 5, name: "n", amount: 5 } );
				q1.sort( "name", "desc" );
				queryDeleteColumn( q1, "id" );
				expect( snapshot( cachedQuery( sql ) ) ).toBe( before );
			} );

			it( title="queries with values that are not immutable (date, struct, array) are still isolated", body=function() {
				// dates and complex values are copied right away, like before
				for ( var source in [ "srcWithDate", "srcWithObject" ] ) {
					var sql = uniqueSql( source, source );
					var q1 = cachedQuery( sql, source );
					var before = snapshot( q1 );
					var q2 = cachedQuery( sql, source );
					expect( cachedFlag( sql, source ) ).toBeTrue();
					var col = listLast( q2.columnList );
					expect( q2.getColumn( col ).isDataShared() ).toBeFalse( "column [#col#] holds values that must be copied" );
					expect( q2.getColumn( "name" ).isDataShared() ).toBeTrue();
					if ( source == "srcWithDate" ) {
						q2.day[ 1 ] = now();
						q2.name[ 1 ] = "X";
						// a date object returned from the cache must not be the one in the cache
						var d = q2.day[ 2 ];
						expect( isDate( d ) ).toBeTrue();
					}
					else {
						q2.obj[ 1 ].a = 99;
						arrayAppend( q2.obj[ 2 ], 3 );
						q2.name[ 1 ] = "X";
					}
					expect( snapshot( cachedQuery( sql, source ) ) ).toBe( before );
				}
			} );

			it( title="a query from the cache can be written to after the cache entry expired or got flushed", body=function() {
				var sql = uniqueSql( "flush" );
				cachedQuery( sql );
				var q = cachedQuery( sql );
				expect( cachedWithinFlush( q ) ).toBeTrue();
				q.name[ 1 ] = "X";
				queryAddRow( q, { id: 4, name: "d", amount: 4 } );
				expect( q.name[ 1 ] ).toBe( "X" );
				expect( q.recordCount ).toBe( 4 );
				var fresh = cachedQuery( sql );
				expect( fresh.recordCount ).toBe( 3 );
				expect( fresh.name[ 1 ] ).toBe( "a" );
			} );

			it( title="the debug column of a cached query does not break the sharing", body=function() {
				var sql = uniqueSql( "debug" );
				cachedQuery( sql );
				var q = cachedQuery( sql );
				var col = q.getColumn( "name" );
				var dc = col._toDebugColumn();
				// the debug column is a view on the data of the column and not of the cache
				expect( col.isDataShared() ).toBeFalse();
				q.name[ 1 ] = "X";
				expect( cachedQuery( sql ).name[ 1 ] ).toBe( "a" );
			} );

			it( title="many threads reading and writing a cached query at the same time", body=function() {
				var sql = uniqueSql( "threads" );
				cachedQuery( sql );
				var before = snapshot( cachedQuery( sql ) );
				var names = [];
				var threadCount = 12;
				for ( var t = 1; t <= threadCount; t++ ) {
					var tn = "ldev1406_#createUniqueId()#_#t#";
					arrayAppend( names, tn );
					thread name=tn t=t sql=sql {
						thread.errors = [];
						try {
							for ( var i = 1; i <= 150; i++ ) {
								var q = cachedQuery( attributes.sql );
								// every thread sees the pristine data
								if ( q.recordCount != 3 || q.name[ 2 ] != "b" || q.amount[ 3 ] != 3.5 ) arrayAppend( thread.errors, "dirty data in thread #attributes.t# loop #i#: #serializeJSON( q )#" );
								if ( ( attributes.t + i ) % 3 == 0 ) {
									// write
									q.name[ 2 ] = "w#attributes.t#";
									queryAddRow( q, { id: 100 + i, name: "w", amount: 1 } );
									if ( q.name[ 2 ] != "w#attributes.t#" || q.recordCount != 4 ) arrayAppend( thread.errors, "own write lost in thread #attributes.t#" );
								}
								else if ( ( attributes.t + i ) % 3 == 1 ) {
									querySort( q, "name", "desc" );
									if ( q.name[ 1 ] != "c" ) arrayAppend( thread.errors, "sort broken in thread #attributes.t#" );
								}
								else {
									var s = serializeJSON( q ); // read only
								}
							}
						}
						catch ( any e ) {
							arrayAppend( thread.errors, e.message );
						}
					}
				}
				thread action="join" name=arrayToList( names ) timeout=120000;
				for ( var tn in names ) {
					var th = cfthread[ tn ];
					expect( th.status ).toBe( "COMPLETED", serializeJSON( th ) );
					expect( th.errors ).toBeEmpty( serializeJSON( th.errors ) );
				}
				expect( snapshot( cachedQuery( sql ) ) ).toBe( before );
			} );

		} );
	}

	private function addMutatorSpec( required string title, required function fn ) {
		it( title="mutator: #arguments.title#", body=function() {
			var sql = uniqueSql( "mut" );
			var q1 = cachedQuery( sql ); // miss, stores the entry
			var pristine = snapshot( q1 );
			var q2 = cachedQuery( sql ); // hit
			expect( cachedFlag( sql ) ).toBeTrue( "second call must come from the cache" );
			expect( snapshot( q2 ) ).toBe( pristine );

			// mutate the one that was put into the cache
			fn( q1 );
			expect( snapshot( cachedQuery( sql ) ) ).toBe( pristine, "changing the first result changed the cache" );

			// mutate a query that came from the cache
			var q3 = cachedQuery( sql );
			fn( q3 );
			var q4 = cachedQuery( sql );
			expect( snapshot( q4 ) ).toBe( pristine, "changing a cached result changed the cache" );
			// the other results handed out earlier are not affected by the change of q3
			expect( snapshot( q2 ) ).toBe( pristine, "changing a cached result changed another result of the same entry" );

			// and again with a mutation of the second hit
			fn( q4 );
			expect( snapshot( cachedQuery( sql ) ) ).toBe( pristine );
		} );
	}

	private string function uniqueSql( required string prefix, string source="src" ) {
		variables.counter++;
		// the cache key is built from the sql, so every spec needs its own one
		var cols = ( arguments.source == "srcWithDate" ) ? "id,name,day" : ( arguments.source == "srcWithObject" ? "id,name,obj" : "id,name,amount" );
		return "select #cols# from #arguments.source# where name <> '#arguments.prefix#_#createUniqueId()#_#variables.counter#'";
	}

	private query function cachedQuery( required string sql, string source="src" ) {
		return queryExecute( arguments.sql, {}, { dbtype: "query", cachedWithin: createTimeSpan( 0, 0, 10, 0 ) } );
	}

	private boolean function cachedFlag( required string sql, string source="src" ) {
		queryExecute( arguments.sql, {}, { dbtype: "query", cachedWithin: createTimeSpan( 0, 0, 10, 0 ), result: "local.r" } );
		return local.r.cached;
	}

	private string function snapshot( required query q ) {
		var rows = [];
		var cols = listToArray( q.columnList );
		arrayAppend( rows, q.columnList & "|" & q.recordCount );
		for ( var r = 1; r <= q.recordCount; r++ ) {
			var cells = [];
			for ( var c in cols ) {
				var v = q[ c ][ r ];
				arrayAppend( cells, isSimpleValue( v ) ? v : serializeJSON( v ) );
			}
			arrayAppend( rows, arrayToList( cells, "," ) );
		}
		return arrayToList( rows, ";" );
	}

}
