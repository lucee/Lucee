component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function run( testResults, testBox ) {
		describe( title="Test suite for jfrBegin()", body=function() {
			it( title="checking jfrBegin() returns event ID", skip=!jfrAvailable(), body=function( currentSpec ) {
				var eventId = jfrBegin( "test", "Test Duration Event" );
				expect( eventId ).toBeTypeOf( "string" );
				jfrCommit( eventId );
			} );

			it( title="checking jfrBegin() with data struct", skip=!jfrAvailable(), body=function( currentSpec ) {
				var eventId = jfrBegin( "database", "Query Test", { query: "SELECT 1", cached: false } );
				expect( eventId ).toBeTypeOf( "string" );
				sleep( 5 );
				jfrCommit( eventId, { success: true, rowCount: 1 } );
			} );
		} );

		describe( title="Test suite for jfrBegin() with named arguments", body=function() {
			it( title="checking jfrBegin() with named category and label", skip=!jfrAvailable(), body=function( currentSpec ) {
				var eventId = jfrBegin( category="test", label="Test Duration Event" );
				expect( eventId ).toBeTypeOf( "string" );
				jfrCommit( eventId );
			} );

			it( title="checking jfrBegin() with named arguments and data struct", skip=!jfrAvailable(), body=function( currentSpec ) {
				var eventId = jfrBegin( category="database", label="Query Test", data={ query: "SELECT 1", cached: false } );
				expect( eventId ).toBeTypeOf( "string" );
				sleep( 5 );
				jfrCommit( eventId, { success: true, rowCount: 1 } );
			} );
		} );
	}
}
