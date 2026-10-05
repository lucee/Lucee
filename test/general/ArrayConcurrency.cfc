component extends="org.lucee.cfml.test.LuceeTestCase" {

	// CFML arrays are synchronized by default (LDEV-2096), but the synchronization used to be per java.util.List
	// call, so compound operations raced: reads threw raw IndexOutOfBoundsException, iteration threw
	// ConcurrentModificationException, duplicate() came back truncated. Each spec runs a writer role against
	// reader roles on one shared array, via parallel arrayEach, and counts what the readers saw go wrong.

	variables.DURATION = 1500;

	function run( testResults, testBox ) {

		describe( title = "Array concurrency, readers against a churning queue", body = function() {

			it( title = "arr[ i ] on a shrinking array only throws the CFML out of range error", body = function( currentSpec ) {
				var arr = [ 1, 2 ];
				var res = race( arr, function( arr ) {
					// size flips between 1 and 2
					arrayPop( arr );
					arrayAppend( arr, 2 );
				}, function( arr, res ) {
					try {
						arr[ 2 ];
					}
					catch ( any e ) {
						if ( e.type != "expression" ) arrayAppend( res.errors, e.type & ": " & e.message );
					}
				} );
				assertEquals( [], res.errors );
			});

			it( title = "for-in over a queue doesn't throw and never yields an element twice", body = function( currentSpec ) {
				var arr = seq( 1, 200 );
				var res = race( arr, queueChurn( 200 ), function( arr, res ) {
					var prev = 0;
					try {
						for ( var v in arr ) {
							if ( v <= prev ) arrayAppend( res.errors, "not increasing: #v# after #prev#" );
							prev = v;
						}
					}
					catch ( any e ) {
						arrayAppend( res.errors, e.type & ": " & e.message );
					}
				} );
				assertEquals( [], res.errors );
			});

			it( title = "each() over a queue doesn't throw and never yields an element twice", body = function( currentSpec ) {
				var arr = seq( 1, 200 );
				var res = race( arr, queueChurn( 200 ), function( arr, res ) {
					var state = { prev: 0 };
					try {
						// each() walks a key snapshot with a live get per key, so a key the array has since shrunk past
						// yields null, as it does single threaded when the closure deletes. The queue is never shorter
						// than 200, so only the snapshot's extra key 201 can do that; a null below it is a torn read
						arrayEach( arr, function( v, i ) {
							if ( isNull( arguments.v ) ) {
								if ( arguments.i <= 200 ) arrayAppend( res.errors, "null element at #arguments.i#" );
							}
							else if ( arguments.v <= state.prev ) arrayAppend( res.errors, "not increasing: #arguments.v# after #state.prev#" );
							else state.prev = arguments.v;
						} );
					}
					catch ( any e ) {
						arrayAppend( res.errors, e.type & ": " & e.message );
					}
				} );
				assertEquals( [], res.errors );
			});

			it( title = "duplicate() of a queue is a complete point in time copy", body = function( currentSpec ) {
				var arr = seq( 1, 200 );
				var res = race( arr, queueChurn( 200 ), function( arr, res ) {
					try {
						var copy = duplicate( arr );
						var len = arrayLen( copy );
						// the writer keeps the queue at 200 or 201 elements, each one more than the last
						if ( len < 200 ) arrayAppend( res.errors, "truncated to #len#" );
						else if ( copy[ len ] - copy[ 1 ] != len - 1 ) arrayAppend( res.errors, "not contiguous: #copy[ 1 ]#..#copy[ len ]# in #len#" );
					}
					catch ( any e ) {
						arrayAppend( res.errors, e.type & ": " & e.message );
					}
				} );
				assertEquals( [], res.errors );
			});

			it( title = "serializeJson() of a queue doesn't throw", body = function( currentSpec ) {
				var arr = seq( 1, 200 );
				var res = race( arr, queueChurn( 200 ), function( arr, res ) {
					try {
						var len = arrayLen( deserializeJson( serializeJson( arr ) ) );
						if ( len < 200 ) arrayAppend( res.errors, "truncated to #len#" );
					}
					catch ( any e ) {
						arrayAppend( res.errors, e.type & ": " & e.message );
					}
				} );
				assertEquals( [], res.errors );
			});

		});

		describe( title = "Array concurrency, writers", body = function() {

			it( title = "parallel arrayAppend keeps every element and adds no padding", body = function( currentSpec ) {
				var arr = [];
				var work = seq( 1, 20000 );
				arrayEach( work, function( v ) {
					arrayAppend( arr, arguments.v );
				}, true, 16 );
				assertEquals( 20000, arrayLen( arr ) );
				arraySort( arr, "numeric" );
				assertEquals( work, arr );
			});

			it( title = "parallel shift drains every element exactly once", body = function( currentSpec ) {
				var arr = seq( 1, 20000 );
				var taken = [ [], [], [], [] ];
				arrayEach( [ 1, 2, 3, 4 ], function( w ) {
					var mine = taken[ arguments.w ];
					while ( true ) {
						var v = arrayShift( arr, "empty" );
						if ( v == "empty" ) break;
						arrayAppend( mine, v );
					}
				}, true, 4 );
				var all = [];
				for ( var t in taken ) arrayAppend( all, t, true );
				arraySort( all, "numeric" );
				assertEquals( seq( 1, 20000 ), all );
				assertEquals( 0, arrayLen( arr ) );
			});

			it( title = "sort with a comparator that reads the array loses no concurrent append", body = function( currentSpec ) {
				var arr = seq( 1, 2000 );
				var appended = { n: 0 };
				arrayEach( [ "sort", "append" ], function( role ) {
					if ( arguments.role == "sort" ) {
						for ( var i = 1; i <= 20; i++ ) {
							arraySort( arr, function( a, b ) {
								// reading the array from the comparator must not deadlock
								arrayLen( arr );
								return arguments.b - arguments.a;
							} );
						}
					}
					else {
						for ( var i = 1; i <= 2000; i++ ) {
							arrayAppend( arr, 2000 + i );
							appended.n++;
						}
					}
				}, true, 2 );
				assertEquals( 4000, arrayLen( arr ) );
				arraySort( arr, "numeric" );
				assertEquals( seq( 1, 4000 ), arr );
			});

		});

		describe( title = "Array serialization", body = function() {

			it( title = "objectSave / objectLoad round trip keeps values and synchronization", body = function( currentSpec ) {
				var arr = [ 1, "two", [ 3 ], { four: 4 } ];
				arrayAppend( arr, nullValue() );
				arrayAppend( arr, 6 );
				var back = objectLoad( objectSave( arr ) );
				assertEquals( 6, arrayLen( back ) );
				assertEquals( serializeJson( arr ), serializeJson( back ) );
				assertEquals( getMetadata( arr ).type, getMetadata( back ).type );

				var unsync = arrayNew( 1, "", false );
				arrayAppend( unsync, 1 );
				assertEquals( getMetadata( unsync ).type, getMetadata( objectLoad( objectSave( unsync ) ) ).type );
			});

		});
	}

	// writer runs until the readers are done, readers each run for DURATION ms
	private struct function race( required array arr, required function writer, required function reader, numeric readers = 3 ) {
		var res = { errors: [], reads: 0 };
		var state = { done: false };
		var roles = [ "writer" ];
		for ( var i = 1; i <= arguments.readers; i++ ) arrayAppend( roles, "reader" );
		var shared = arguments.arr;
		var writeOp = arguments.writer;
		var readOp = arguments.reader;
		var duration = variables.DURATION;
		var readersLeft = { n: arguments.readers };
		arrayEach( roles, function( role ) {
			if ( arguments.role == "writer" ) {
				var end = getTickCount() + duration * 4;
				while ( !state.done && getTickCount() < end ) writeOp( shared );
			}
			else {
				var mine = { errors: [] };
				var end = getTickCount() + duration;
				var n = 0;
				while ( getTickCount() < end ) {
					readOp( shared, mine );
					n++;
				}
				lock name="ArrayConcurrency.race" type="exclusive" timeout=10 {
					arrayAppend( res.errors, mine.errors, true );
					res.reads += n;
					readersLeft.n--;
					if ( readersLeft.n == 0 ) state.done = true;
				}
			}
		}, true, arrayLen( roles ) );
		systemOutput( "#res.reads# reads, #arrayLen( res.errors )# errors", true );
		// keep the assertion message readable
		if ( arrayLen( res.errors ) > 5 ) {
			var total = arrayLen( res.errors );
			res.errors = arraySlice( res.errors, 1, 5 );
			arrayAppend( res.errors, "... #total# in total" );
		}
		return res;
	}

	// append the next value, drop the front: the queue stays at size or size + 1, always ascending by one
	private function queueChurn( required numeric size ) {
		var next = { v: arguments.size + 1 };
		return function( arr ) {
			arrayAppend( arr, next.v++ );
			arrayDeleteAt( arr, 1 );
		};
	}

	private array function seq( required numeric from, required numeric to ) {
		var arr = [];
		for ( var i = arguments.from; i <= arguments.to; i++ ) arrayAppend( arr, i );
		return arr;
	}
}
