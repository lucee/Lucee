component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "LDEV-4397 runAsync uses a shared executor", function() {

			it( title="many concurrent runAsync calls complete with the correct result", body=function( currentSpec ) {
				var count = 2000;
				var futures = [];
				loop from=1 to=count index="local.i" {
					arrayAppend( futures, runAsync( task( i * 2 ) ) );
				}
				loop from=1 to=count index="local.i" {
					expect( futures[ i ].get() ).toBe( i * 2 );
				}
			});

			it( title="many concurrent runAsync calls that wait for a while all complete", body=function( currentSpec ) {
				var count = 500;
				var futures = [];
				loop from=1 to=count index="local.i" {
					arrayAppend( futures, runAsync( slowTask( i ) ) );
				}
				loop from=1 to=count index="local.i" {
					expect( futures[ i ].get() ).toBe( i );
				}
			});

			it( title="a burst of concurrent runAsync calls does not create a platform thread per call", body=function( currentSpec ) {
				var mx = createObject( "java", "java.lang.management.ManagementFactory" ).getThreadMXBean();
				var before = mx.getThreadCount();
				var peak = before;
				var count = 3000;
				var futures = [];
				loop from=1 to=count index="local.i" {
					arrayAppend( futures, runAsync( slowTask( i ) ) );
					if ( i mod 100 eq 0 ) peak = max( peak, mx.getThreadCount() );
				}
				sleep( 100 );
				peak = max( peak, mx.getThreadCount() );
				loop from=1 to=count index="local.i" {
					expect( futures[ i ].get() ).toBe( i );
				}
				// before LDEV-4397 every call started its own platform thread
				expect( peak - before ).toBeLT( 500 );
			});

			it( title="chained then calls pass the result on", body=function( currentSpec ) {
				var f = runAsync( () => 1 )
					.then( ( v ) => v + 1 )
					.then( ( v ) => v * 10 )
					.then( ( v ) => "result:" & v );
				expect( f.get() ).toBe( "result:20" );
			});

			it( title="many chained then calls running at the same time do not block each other", body=function( currentSpec ) {
				var count = 500;
				var futures = [];
				loop from=1 to=count index="local.i" {
					arrayAppend( futures, runAsync( task( i ) ).then( ( v ) => v + 1 ).then( ( v ) => v + 1 ) );
				}
				loop from=1 to=count index="local.i" {
					expect( futures[ i ].get() ).toBe( i + 2 );
				}
			});

			it( title="a runAsync that waits for another runAsync does not dead lock", body=function( currentSpec ) {
				var count = 200;
				var futures = [];
				loop from=1 to=count index="local.i" {
					arrayAppend( futures, runAsync( nestedTask( i ) ) );
				}
				loop from=1 to=count index="local.i" {
					expect( futures[ i ].get( 60000 ) ).toBe( i + 1000 );
				}
			});

			it( title="an error in runAsync is thrown by get", body=function( currentSpec ) {
				var f = runAsync( function() {
					throw( message="LDEV4397 boom", type="LDEV4397Type" );
				} );
				expect( function() {
					f.get();
				} ).toThrow();
				var msg = "";
				try {
					f.get();
				}
				catch ( any e ) {
					msg = e.message & " " & e.stacktrace;
				}
				expect( msg ).toInclude( "LDEV4397 boom" );
			});

			it( title="error handler receives the error and its result is returned", body=function( currentSpec ) {
				var f = runAsync( function() {
					throw( message="LDEV4397 handled", type="LDEV4397Type" );
				} ).error( ( e ) => "handled:" & e.message );
				expect( f.get() ).toBe( "handled:LDEV4397 handled" );
			});

			it( title="error handler is not called when there is no error", body=function( currentSpec ) {
				var f = runAsync( () => "fine" ).error( ( e ) => "handled" );
				expect( f.get() ).toBe( "fine" );
			});

			it( title="many failing runAsync calls with an error handler are all handled", body=function( currentSpec ) {
				var count = 500;
				var futures = [];
				loop from=1 to=count index="local.i" {
					arrayAppend( futures, runAsync( failingTask( i ) ).error( ( e ) => e.message ) );
				}
				loop from=1 to=count index="local.i" {
					expect( futures[ i ].get() ).toBe( "fail " & i );
				}
			});

			it( title="a step sees the request scope of the caller", body=function( currentSpec ) {
				request.ldev4397 = "req";
				var results = [];
				loop from=1 to=50 index="local.i" {
					arrayAppend( results, runAsync( () => structKeyExists( request, "ldev4397" ) ) );
				}
				loop array=results item="local.f" {
					expect( f.get() ).toBeTrue();
				}
				structDelete( request, "ldev4397" );
			});

		});
	}

	private function task( required any value ) {
		return function() {
			return value;
		};
	}

	private function slowTask( required any value ) {
		return function() {
			sleep( 200 );
			return value;
		};
	}

	private function nestedTask( required numeric value ) {
		return function() {
			return runAsync( function() {
				return value;
			} ).get() + 1000;
		};
	}

	private function failingTask( required numeric value ) {
		return function() {
			throw( message="fail " & value );
		};
	}

}
