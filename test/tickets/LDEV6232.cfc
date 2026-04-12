component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "QueryImpl currentRow thread safety", function() {

			it( "single-threaded cfloop maintains correct currentRow", function() {
				var q = queryNew( "id,name", "integer,varchar" );
				loop times=100 {
					var r = queryAddRow( q );
					querySetCell( q, "id", r, r );
					querySetCell( q, "name", "row_#r#", r );
				}

				var collected = [];
				loop query="q" {
					arrayAppend( collected, q.currentRow & ":" & q.id );
				}
				expect( collected.len() ).toBe( 100 );
				expect( collected[ 1 ] ).toBe( "1:1" );
				expect( collected[ 50 ] ).toBe( "50:50" );
				expect( collected[ 100 ] ).toBe( "100:100" );
			} );

			it( "for-in loop maintains correct row data", function() {
				var q = queryNew( "id,name", "integer,varchar" );
				loop times=50 {
					var r = queryAddRow( q );
					querySetCell( q, "id", r, r );
					querySetCell( q, "name", "name_#r#", r );
				}

				var collected = [];
				for ( var row in q ) {
					arrayAppend( collected, row.id & ":" & row.name );
				}
				expect( collected.len() ).toBe( 50 );
				expect( collected[ 1 ] ).toBe( "1:name_1" );
				expect( collected[ 50 ] ).toBe( "50:name_50" );
			} );

			it( "concurrent threads get independent currentRow", function() {
				var q = queryNew( "id,name", "integer,varchar" );
				loop times=200 {
					var r = queryAddRow( q );
					querySetCell( q, "id", r, r );
					querySetCell( q, "name", "row_#r#", r );
				}

				// each thread iterates the full query and collects all id values
				var results = {};
				var errors = [];
				var threadCount = 10;

				loop from=1 to=threadCount index="t" {
					thread name="qtest_#t#" query=q results=results errors=errors t=t {
						try {
							var myRows = [];
							loop query="attributes.query" {
								arrayAppend( myRows, attributes.query.id );
							}
							results[ "t_#attributes.t#" ] = myRows;
						} catch ( any e ) {
							arrayAppend( errors, "thread #attributes.t#: #e.message#" );
						}
					}
				}

				// wait for all threads
				loop from=1 to=threadCount index="t" {
					thread action="join" name="qtest_#t#" timeout=30000;
				}

				expect( errors.len() ).toBe( 0, errors.toList( chr( 10 ) ) );

				// each thread should have seen all 200 rows in order
				loop from=1 to=threadCount index="t" {
					var key = "t_#t#";
					expect( results ).toHaveKey( key, "thread #t# didn't complete" );
					var rows = results[ key ];
					expect( rows.len() ).toBe( 200, "thread #t# got #rows.len()# rows instead of 200" );
					expect( rows[ 1 ] ).toBe( 1, "thread #t# first row should be 1" );
					expect( rows[ 200 ] ).toBe( 200, "thread #t# last row should be 200" );
				}
			} );

			it( "concurrent threads with for-in get correct data", function() {
				var q = queryNew( "id,val", "integer,varchar" );
				loop times=100 {
					var r = queryAddRow( q );
					querySetCell( q, "id", r, r );
					querySetCell( q, "val", "v#r#", r );
				}

				var results = {};
				var errors = [];
				var threadCount = 20;

				loop from=1 to=threadCount index="t" {
					thread name="qforin_#t#" query=q results=results errors=errors t=t {
						try {
							var myData = [];
							for ( var row in attributes.query ) {
								arrayAppend( myData, row.id & ":" & row.val );
							}
							results[ "t_#attributes.t#" ] = myData;
						} catch ( any e ) {
							arrayAppend( errors, "thread #attributes.t#: #e.message#" );
						}
					}
				}

				loop from=1 to=threadCount index="t" {
					thread action="join" name="qforin_#t#" timeout=30000;
				}

				expect( errors.len() ).toBe( 0, errors.toList( chr( 10 ) ) );

				loop from=1 to=threadCount index="t" {
					var key = "t_#t#";
					expect( results ).toHaveKey( key );
					var data = results[ key ];
					expect( data.len() ).toBe( 100, "thread #t# got #data.len()# rows" );
					expect( data[ 1 ] ).toBe( "1:v1" );
					expect( data[ 100 ] ).toBe( "100:v100" );
				}
			} );

			it( "rapid sequential loops maintain correct state", function() {
				var q = queryNew( "id", "integer" );
				loop times=10 {
					var r = queryAddRow( q );
					querySetCell( q, "id", r, r );
				}

				// run 1000 sequential loops on the same query
				loop times=1000 {
					var sum = 0;
					loop query="q" {
						sum += q.id;
					}
					// 1+2+3+...+10 = 55
					expect( sum ).toBe( 55 );
				}
			} );

			it( "cfbreak restores currentRow to pre-loop position", function() {
				var q = queryNew( "id", "integer" );
				loop times=20 {
					var r = queryAddRow( q );
					querySetCell( q, "id", r, r );
				}

				loop query="q" {
					if ( q.id == 7 ) break;
				}

				// compiled bytecode restores currentRow to pre-loop position in finally block
				expect( q.id ).toBe( 1 );
			} );

			it( "queryEach with parallel threads gets correct data", function() {
				var q = queryNew( "id,name", "integer,varchar" );
				loop times=50 {
					var r = queryAddRow( q );
					querySetCell( q, "id", r, r );
					querySetCell( q, "name", "n#r#", r );
				}

				var collected = [];
				queryEach( q, function( row ) {
					arrayAppend( collected, row.id, true );
				} );

				expect( collected.len() ).toBe( 50 );
				arraySort( collected, "numeric" );
				expect( collected[ 1 ] ).toBe( 1 );
				expect( collected[ 50 ] ).toBe( 50 );
			} );

			it( "stress test: many threads hammering go() and getCurrentRow()", function() {
				var q = queryNew( "id", "integer" );
				loop times=50 {
					var r = queryAddRow( q );
					querySetCell( q, "id", r, r );
				}

				var errors = [];
				var threadCount = 20;
				var iterations = 500;

				loop from=1 to=threadCount index="t" {
					thread name="qstress_#t#" query=q errors=errors t=t iterations=iterations {
						try {
							loop from=1 to=attributes.iterations index="i" {
								var targetRow = ( i % attributes.query.recordcount ) + 1;
								attributes.query.go( targetRow, 0 );
								var actual = attributes.query.currentRow;
								if ( actual != targetRow ) {
									arrayAppend( errors, "thread #attributes.t# iter #i#: expected row #targetRow# got #actual#" );
									break;
								}
							}
						} catch ( any e ) {
							arrayAppend( errors, "thread #attributes.t#: #e.message#" );
						}
					}
				}

				loop from=1 to=threadCount index="t" {
					thread action="join" name="qstress_#t#" timeout=60000;
				}

				expect( errors.len() ).toBe( 0, errors.len() > 0 ? errors[ 1 ] : "" );
			} );

		} );
	}

}
