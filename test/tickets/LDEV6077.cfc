component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title = "LDEV-6077: ParentException lazy creation in closure functions", body = function() {

			it( title = "each() exception in synchronous mode", body = function( currentSpec ) {
				var caught = false;
				var message = "";
				try {
					var arr = [ 1, 2, 3, 4, 5 ];
					arr.each( function( item ) {
						if ( item == 3 ) {
							throw( message="Sync error at item 3" );
						}
					}, false );
				} catch ( any e ) {
					caught = true;
					message = e.message;
				}
				expect( caught ).toBeTrue();
				expect( message ).toInclude( "Sync error" );
			});

			it( title = "each() exception in parallel mode", body = function( currentSpec ) {
				var caught = false;
				var message = "";
				var hasCause = false;
				var causeMessage = "";
				try {
					var arr = [ 1, 2, 3, 4, 5 ];
					arr.each( function( item ) {
						if ( item == 3 ) {
							throw( message="Parallel error at item 3" );
						}
					}, true );
				} catch ( any e ) {
					caught = true;
					message = e.message;
					hasCause = !isNull( e.cause );
					if ( hasCause && isStruct( e.cause ) && structKeyExists( e.cause, "message" ) ) {
						causeMessage = e.cause.message;
					}
				}
				expect( caught ).toBeTrue();
				expect( message ).toInclude( "Parallel error" );
				// ParentException should be attached as cause in parallel mode
				expect( hasCause ).toBeTrue();
				expect( causeMessage ).toInclude( "parent thread stacktrace" );
			});

			it( title = "map() exception handling", body = function( currentSpec ) {
				var caught = false;
				var message = "";
				try {
					var arr = [ 1, 2, 3, 4, 5 ];
					var result = arr.map( function( item ) {
						if ( item == 4 ) {
							throw( message="Map error at item 4" );
						}
						return item * 2;
					}, false );
				} catch ( any e ) {
					caught = true;
					message = e.message;
				}
				expect( caught ).toBeTrue();
				expect( message ).toInclude( "Map error" );
			});

			it( title = "filter() exception handling", body = function( currentSpec ) {
				var caught = false;
				var message = "";
				try {
					var arr = [ 1, 2, 3, 4, 5 ];
					var result = arr.filter( function( item ) {
						if ( item == 2 ) {
							throw( message="Filter error at item 2" );
						}
						return item % 2 == 0;
					}, false );
				} catch ( any e ) {
					caught = true;
					message = e.message;
				}
				expect( caught ).toBeTrue();
				expect( message ).toInclude( "Filter error" );
			});

			it( title = "reduce() exception handling", body = function( currentSpec ) {
				var caught = false;
				var message = "";
				try {
					var arr = [ 1, 2, 3, 4, 5 ];
					var result = arr.reduce( function( acc, item ) {
						if ( item == 3 ) {
							throw( message="Reduce error at item 3" );
						}
						return acc + item;
					}, 0 );
				} catch ( any e ) {
					caught = true;
					message = e.message;
				}
				expect( caught ).toBeTrue();
				expect( message ).toInclude( "Reduce error" );
			});

			it( title = "struct.each() exception handling", body = function( currentSpec ) {
				var caught = false;
				var message = "";
				try {
					var struct = { a: 1, b: 2, c: 3, d: 4 };
					struct.each( function( key, value ) {
						if ( key == "c" ) {
							throw( message="Struct error at key c" );
						}
					}, false );
				} catch ( any e ) {
					caught = true;
					message = e.message;
				}
				expect( caught ).toBeTrue();
				expect( message ).toInclude( "Struct error" );
			});

			it( title = "query.each() exception handling", body = function( currentSpec ) {
				var caught = false;
				var message = "";
				try {
					var qry = queryNew( "id,name", "integer,varchar", [
						{ id: 1, name: "Alice" },
						{ id: 2, name: "Bob" },
						{ id: 3, name: "Charlie" }
					] );
					qry.each( function( row, rowNumber ) {
						if ( rowNumber == 2 ) {
							throw( message="Query error at row 2" );
						}
					}, false );
				} catch ( any e ) {
					caught = true;
					message = e.message;
				}
				expect( caught ).toBeTrue();
				expect( message ).toInclude( "Query error" );
			});

			it( title = "nested each() exception handling", body = function( currentSpec ) {
				var caught = false;
				var message = "";
				var processed = 0;
				try {
					var outer = [ 1, 2, 3 ];
					outer.each( function( outerItem ) {
						var inner = [ 10, 20, 30 ];
						inner.each( function( innerItem ) {
							processed++;
							if ( outerItem == 2 && innerItem == 20 ) {
								throw( message="Nested error at outer=2, inner=20" );
							}
						}, false );
					}, false );
				} catch ( any e ) {
					caught = true;
					message = e.message;
				}
				expect( caught ).toBeTrue();
				expect( message ).toInclude( "Nested error" );
				expect( processed ).toBe( 5 ); // Should have processed 3 + 2 items before error
			});

			it( title = "stack trace captured in exceptions", body = function( currentSpec ) {
				var caught = false;
				var stackTrace = "";
				try {
					var arr = [ 1, 2, 3 ];
					arr.each( function( item ) {
						if ( item == 2 ) {
							throw( message="Stack trace test" );
						}
					}, false );
				} catch ( any e ) {
					caught = true;
					stackTrace = e.stacktrace;
				}
				expect( caught ).toBeTrue();
				expect( stackTrace ).toInclude( "each", "Stack should contain 'each' reference" );
				expect( len( stackTrace ) ).toBeGT( 0, "Stack trace should not be empty" );
			});

			it( title = "parallel exception with multiple threads", body = function( currentSpec ) {
				var caught = false;
				var message = "";
				try {
					var arr = [ 1, 2, 3, 4, 5 ];
					arr.each( function( item ) {
						if ( item == 3 ) {
							throw( message="Parallel stress error at item " & item );
						}
						sleep( 5 );
					}, true );
				} catch ( any e ) {
					caught = true;
					message = e.message;
				}
				expect( caught ).toBeTrue();
				expect( message ).toInclude( "Parallel stress" );
			});

		});
	}
}
