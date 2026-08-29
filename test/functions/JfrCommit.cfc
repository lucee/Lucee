component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function run( testResults, testBox ) {
		describe( title="Test suite for jfrCommit()", body=function() {
			it( title="checking jfrCommit() completes duration event", skip=!jfrAvailable(), body=function( currentSpec ) {
				expect( function() {
					var eventId = jfrBegin( "test", "Test Duration Event" );
					sleep( 10 );
					jfrCommit( eventId );
				} ).notToThrow();
			} );

			it( title="checking jfrCommit() with additional data", skip=!jfrAvailable(), body=function( currentSpec ) {
				expect( function() {
					var eventId = jfrBegin( "test", "Operation" );
					jfrCommit( eventId, { success: true, result: "completed" } );
				} ).notToThrow();
			} );

			it( title="checking jfrCommit() with exception data", skip=!jfrAvailable(), body=function( currentSpec ) {
				expect( function() {
					var eventId = jfrBegin( "test", "Failing Operation" );
					try {
						throw( message="Test error", type="TestException" );
					}
					catch( TestException e ) {
						jfrCommit( eventId, { success: false, error: e.message } );
					}
				} ).notToThrow();
			} );
		} );

		describe( title="Test suite for jfrCommit() with named arguments", body=function() {
			it( title="checking jfrCommit() with named eventId", skip=!jfrAvailable(), body=function( currentSpec ) {
				expect( function() {
					var eventId = jfrBegin( "test", "Test Duration Event" );
					sleep( 10 );
					jfrCommit( eventId=eventId );
				} ).notToThrow();
			} );

			it( title="checking jfrCommit() with named eventId and data", skip=!jfrAvailable(), body=function( currentSpec ) {
				expect( function() {
					var eventId = jfrBegin( "test", "Operation" );
					jfrCommit( eventId=eventId, data={ success: true, result: "completed" } );
				} ).notToThrow();
			} );

			it( title="checking jfrCommit() with named arguments and exception data", skip=!jfrAvailable(), body=function( currentSpec ) {
				expect( function() {
					var eventId = jfrBegin( "test", "Failing Operation" );
					try {
						throw( message="Test error", type="TestException" );
					}
					catch( TestException e ) {
						jfrCommit( eventId=eventId, data={ success: false, error: e.message } );
					}
				} ).notToThrow();
			} );
		} );
	}
}
