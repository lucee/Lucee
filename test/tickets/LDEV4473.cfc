component extends="org.lucee.cfml.test.LuceeTestCase" labels="loop" {

	function run( testResults, testBox ) {

		describe( "LDEV-4473: for loops with a plain counter", function() {

			it( "counts up", function() {
				var r = [];
				for ( var i = 1; i <= 5; i++ ) r.append( i );
				expect( r.toList() ).toBe( "1,2,3,4,5" );
				expect( i ).toBe( 6 );

				r = [];
				for ( var j = 1; j < 5; j++ ) r.append( j );
				expect( r.toList() ).toBe( "1,2,3,4" );
				expect( j ).toBe( 5 );

				r = [];
				for ( var k = 1; k <= 5; ++k ) r.append( k );
				expect( r.toList() ).toBe( "1,2,3,4,5" );
			});

			it( "counts down", function() {
				var r = [];
				for ( var i = 5; i >= 1; i-- ) r.append( i );
				expect( r.toList() ).toBe( "5,4,3,2,1" );
				expect( i ).toBe( 0 );

				r = [];
				for ( var j = 5; j > 1; j-- ) r.append( j );
				expect( r.toList() ).toBe( "5,4,3,2" );
				expect( j ).toBe( 1 );
			});

			it( "uses a step", function() {
				var r = [];
				for ( var i = 0; i < 10; i += 3 ) r.append( i );
				expect( r.toList() ).toBe( "0,3,6,9" );
				expect( i ).toBe( 12 );

				r = [];
				for ( var j = 10; j > 0; j -= 4 ) r.append( j );
				expect( r.toList() ).toBe( "10,6,2" );
				expect( j ).toBe( -2 );
			});

			it( "uses decimal start and step", function() {
				var r = [];
				for ( var i = 0.5; i < 2; i += 0.5 ) r.append( i );
				expect( r.toList() ).toBe( "0.5,1,1.5" );

				r = [];
				for ( var j = 1; j <= 2; j += 0.25 ) r.append( j );
				expect( r.toList() ).toBe( "1,1.25,1.5,1.75,2" );
			});

			it( "does not run when the condition is false from the start", function() {
				var r = [];
				for ( var i = 5; i <= 1; i++ ) r.append( i );
				expect( r ).toBeEmpty();
				expect( i ).toBe( 5 );
			});

			it( "starts with an integer returned by a function", function() {
				var r = [];
				for ( var i = len( "abc" ); i <= 5; i++ ) r.append( i );
				expect( r.toList() ).toBe( "3,4,5" );
			});

			it( "starts with a numeric string", function() {
				var r = [];
				for ( var i = "2"; i <= 4; i++ ) r.append( i );
				expect( r.toList() ).toBe( "2,3,4" );
			});

			it( "starts with a non numeric string like before", function() {
				// "abc" <= 3 is a string comparison (false), so the body is never executed and the counter is untouched
				var r = [];
				for ( var i = "abc"; i <= 3; i++ ) r.append( i );
				expect( r ).toBeEmpty();
				expect( i ).toBe( "abc" );

				// the body runs once, then the counter can not be incremented, this has to throw as before
				var count = 0;
				expect( function() {
					for ( var j = "abc"; j >= "0"; j++ ) {
						count++;
					}
				}).toThrow();
				expect( count ).toBe( 1 );
			});

			it( "compares with a string bound", function() {
				var r = [];
				for ( var i = 1; i <= "3"; i++ ) r.append( i );
				expect( r.toList() ).toBe( "1,2,3" );
			});

			it( "evaluates the bound in every iteration", function() {
				var r = [];
				var n = 3;
				for ( var i = 1; i <= n; i++ ) {
					r.append( i );
					if ( i == 2 && n == 3 ) n = 5;
				}
				expect( r.toList() ).toBe( "1,2,3,4,5" );

				r = [];
				var arr = [ 1, 2, 3 ];
				for ( var i = 1; i <= arr.len(); i++ ) {
					r.append( arr[ i ] );
					if ( i == 1 ) arr.append( 9 );
				}
				expect( r.toList() ).toBe( "1,2,3,9" );

				r = [];
				var arr2 = [ 1, 2, 3 ];
				for ( var i = 1; i <= arrayLen( arr2 ); i++ ) {
					r.append( arr2[ i ] );
					if ( i == 2 ) arrayDeleteAt( arr2, 3 );
				}
				expect( r.toList() ).toBe( "1,2" );
			});

			it( "falls back when the body changes the counter", function() {
				var r = [];
				for ( var i = 1; i <= 6; i++ ) {
					i = i + 1;
					r.append( i );
				}
				expect( r.toList() ).toBe( "2,4,6" );

				r = [];
				for ( var j = 1; j <= 6; j++ ) {
					j += 2;
					r.append( j );
				}
				expect( r.toList() ).toBe( "3,6" );

				r = [];
				for ( var k = 1; k <= 6; k++ ) {
					if ( k == 2 ) k++;
					r.append( k );
				}
				expect( r.toList() ).toBe( "1,3,4,5,6" );
			});

			it( "falls back when a closure changes the counter", function() {
				var r = [];
				var i = 0;
				var skip = function() {
					i += 10;
				};
				for ( i = 1; i <= 25; i++ ) {
					r.append( i );
					if ( i == 2 ) skip();
				}
				expect( r.toList() ).toBe( "1,2,13,14,15,16,17,18,19,20,21,22,23,24,25" );
			});

			it( "falls back when the counter is changed in a nested loop", function() {
				var r = [];
				for ( var i = 1; i <= 5; i++ ) {
					for ( i = i; i < 3; i++ ) {
						r.append( "i" & i );
					}
					r.append( i );
				}
				expect( r.toList() ).toBe( "i1,i2,3,4,5" );
			});

			it( "falls back when the counter is changed by name", function() {
				var r = [];
				for ( var i = 1; i <= 6; i++ ) {
					r.append( i );
					if ( i == 2 ) setVariable( "i", 4 );
				}
				expect( r.toList() ).toBe( "1,2,5,6" );

				r = [];
				for ( var j = 1; j <= 6; j++ ) {
					r.append( j );
					if ( j == 2 ) evaluate( "j = 4" );
				}
				expect( r.toList() ).toBe( "1,2,5,6" );
			});

			it( "falls back when the counter is changed by a function", function() {
				var r = [];
				variables.counterFromFunction = 0;
				for ( variables.counterFromFunction = 1; variables.counterFromFunction <= 6; variables.counterFromFunction++ ) {
					r.append( variables.counterFromFunction );
					if ( variables.counterFromFunction == 2 ) bumpCounter();
				}
				expect( r.toList() ).toBe( "1,2,5,6" );
			});

			it( "supports continue", function() {
				var r = [];
				for ( var i = 1; i <= 10; i++ ) {
					if ( i % 2 == 0 ) continue;
					r.append( i );
				}
				expect( r.toList() ).toBe( "1,3,5,7,9" );
				expect( i ).toBe( 11 );
			});

			it( "supports break", function() {
				var r = [];
				for ( var i = 1; i <= 10; i++ ) {
					if ( i > 4 ) break;
					r.append( i );
				}
				expect( r.toList() ).toBe( "1,2,3,4" );
				expect( i ).toBe( 5 );
			});

			it( "supports continue and break in try catch and switch", function() {
				var r = [];
				for ( var i = 1; i <= 6; i++ ) {
					try {
						if ( i == 2 ) continue;
						if ( i == 5 ) break;
						switch ( i ) {
							case 3:
								r.append( "three" );
								break;
							default:
								r.append( i );
						}
					}
					catch ( any e ) {
						r.append( "error" );
					}
				}
				expect( r.toList() ).toBe( "1,three,4" );
			});

			it( "supports labels", function() {
				var r = [];
				outer: for ( var i = 1; i <= 4; i++ ) {
					for ( var j = 1; j <= 3; j++ ) {
						if ( i == 2 ) continue outer;
						if ( i == 4 ) break outer;
						r.append( i & j );
					}
				}
				expect( r.toList() ).toBe( "11,12,13,31,32,33" );
			});

			it( "supports nested loops", function() {
				var r = [];
				for ( var i = 1; i <= 3; i++ ) {
					for ( var j = i; j <= 3; j++ ) {
						r.append( i & j );
					}
				}
				expect( r.toList() ).toBe( "11,12,13,22,23,33" );

				r = [];
				for ( var i = 3; i >= 1; i-- ) {
					for ( var j = 1; j <= i; j++ ) {
						if ( j == 2 ) continue;
						r.append( i & j );
					}
				}
				expect( r.toList() ).toBe( "31,33,21,11" );
			});

			it( "supports a return in the loop", function() {
				expect( returnInLoop() ).toBe( "2" );
			});

			it( "supports local scoped counters", function() {
				var r = [];
				for ( local.i = 1; local.i <= 3; local.i++ ) r.append( local.i );
				expect( r.toList() ).toBe( "1,2,3" );
				expect( local.i ).toBe( 4 );
			});

			it( "supports variables scoped counters", function() {
				var r = [];
				for ( variables.scopedCounter = 1; variables.scopedCounter <= 3; variables.scopedCounter++ ) r.append( variables.scopedCounter );
				expect( r.toList() ).toBe( "1,2,3" );
				expect( variables.scopedCounter ).toBe( 4 );
			});

			it( "supports unscoped counters", function() {
				expect( unscopedCounter() ).toBe( "1,2,3 4" );
			});

			it( "supports a counter reused in a second loop", function() {
				var r = [];
				for ( var i = 1; i <= 2; i++ ) r.append( i );
				for ( i = 10; i <= 11; i++ ) r.append( i );
				for ( i = 3; i >= 2; i-- ) r.append( i );
				expect( r.toList() ).toBe( "1,2,10,11,3,2" );
			});

			it( "supports a large counter", function() {
				var c = 0;
				for ( var i = 1; i <= 100000; i++ ) c += i;
				expect( c ).toBe( 5000050000 );
			});

			it( "supports a loop in a function called with a counter that is an argument", function() {
				expect( loopWithArgument( 4 ) ).toBe( "1,2,3,4" );
			});

			it( "throws for an undefined bound like before", function() {
				expect( function() {
					for ( var i = 1; i <= doesNotExistBound; i++ ) {
					}
				}).toThrow();
			});
		});
	}

	private function bumpCounter() {
		variables.counterFromFunction += 2;
	}

	private function returnInLoop() {
		for ( var i = 1; i <= 3; i++ ) {
			if ( i == 2 ) return i;
		}
		return "none";
	}

	private function unscopedCounter() {
		var r = [];
		for ( i = 1; i <= 3; i++ ) r.append( i );
		return r.toList() & " " & i;
	}

	private function loopWithArgument( required numeric n ) {
		var r = [];
		for ( var i = 1; i <= arguments.n; i++ ) r.append( i );
		return r.toList();
	}
}
