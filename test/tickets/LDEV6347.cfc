component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title = "LDEV-6347: cfthread parent stack capture is minimal", body = function() {

			it( title = "cfthread that throws produces a ParentException cause", body = function() {
				var threadName = "ldev6347_" & getTickCount() & "_" & randRange( 1, 99999 );
				thread name="#threadName#" {
					throw( type="LDEV6347Test", message="thread body threw" );
				}
				thread name="#threadName#" action="join";

				expect( isNull( cfthread[ threadName ].error ) ).toBeFalse();

				try {
					throw( object = cfthread[ threadName ].error );
				} catch ( any e ) {
					expect( e.message ).toBe( "thread body threw" );
					expect( isNull( e.cause ) ).toBeFalse();
					expect( e.cause.message ).toBe( "parent thread stacktrace" );
				}
			} );

			it( title = "cause.tagcontext contains the spawn-site template", body = function() {
				var threadName = "ldev6347_" & getTickCount() & "_" & randRange( 1, 99999 );
				thread name="#threadName#" {
					throw( type="LDEV6347Test", message="thread body threw" );
				}
				thread name="#threadName#" action="join";

				try {
					throw( object = cfthread[ threadName ].error );
				} catch ( any e ) {
					expect( arrayLen( e.cause.tagcontext ) ).toBeGTE( 1 );
					expect( e.cause.tagcontext[ 1 ].template ).toInclude( "LDEV6347" );
				}
			} );

			// The C3-critical assertion: cause's raw Java stack should be
			// bounded to a handful of synthetic frames, not the full ~40-frame
			// stack that fillInStackTrace() captures pre-fix.
			it( title = "cause's Java stack is bounded (synthetic, not full)", body = function() {
				var threadName = "ldev6347_" & getTickCount() & "_" & randRange( 1, 99999 );
				thread name="#threadName#" {
					throw( type="LDEV6347Test", message="thread body threw" );
				}
				thread name="#threadName#" action="join";

				try {
					throw( object = cfthread[ threadName ].error );
				} catch ( any e ) {
					var rawCause = e.getPageException().getCause();
					expect( isNull( rawCause ) ).toBeFalse();
					expect( rawCause.getClass().getName() ).toBe( "lucee.runtime.exp.ParentException" );

					var stackDepth = arrayLen( rawCause.getStackTrace() );
					expect( stackDepth ).toBeLT( 10 );
				}
			} );

		} );
	}

}
