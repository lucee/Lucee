component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function run( testResults, testBox ) {
		describe( title="Test suite for JFR BIF functions", body=function() {
			it( title="checking jfrEnabled() returns boolean", body=function( currentSpec ) {
				var enabled = jfrEnabled();
				expect( enabled ).toBeTypeOf( "boolean" );
			} );

			it( title="checking jfrEmit() with category and label", body=function( currentSpec ) {
				expect( function() {
					jfrEmit( "test", "Test Event" );
				} ).notToThrow();
			} );

			it( title="checking jfrEmit() with data struct", body=function( currentSpec ) {
				expect( function() {
					jfrEmit( "test", "Test Event with Data", { foo: "bar", count: 42 } );
				} ).notToThrow();
			} );

			it( title="checking jfrBegin() and jfrCommit()", skip=!jfrAvailable(), body=function( currentSpec ) {
				expect( function() {
					var eventId = jfrBegin( "test", "Test Duration Event" );
					expect( eventId ).toBeTypeOf( "string" );

					sleep( 10 );

					jfrCommit( eventId );
				} ).notToThrow();
			} );

			it( title="checking jfrBegin() and jfrCommit() with data", skip=!jfrAvailable(), body=function( currentSpec ) {
				expect( function() {
					var eventId = jfrBegin( "database", "Query Test", { query: "SELECT 1", cached: false } );

					sleep( 5 );

					jfrCommit( eventId, { success: true, rowCount: 1 } );
				} ).notToThrow();
			} );

			it( title="checking jfrBegin() and jfrCommit() with exception", skip=!jfrAvailable(), body=function( currentSpec ) {
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
	}
}
