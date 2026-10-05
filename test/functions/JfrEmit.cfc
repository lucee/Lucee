component extends="org.lucee.cfml.test.LuceeTestCase" labels="jfr" {

	function run( testResults, testBox ) {
		describe( title="Test suite for jfrEmit()", body=function() {
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
		} );

		describe( title="Test suite for jfrEmit() with named arguments", body=function() {
			it( title="checking jfrEmit() with named category and label", body=function( currentSpec ) {
				expect( function() {
					jfrEmit( category="test", label="Test Event" );
				} ).notToThrow();
			} );

			it( title="checking jfrEmit() with named arguments and data struct", body=function( currentSpec ) {
				expect( function() {
					jfrEmit( category="test", label="Test Event with Data", data={ foo: "bar", count: 42 } );
				} ).notToThrow();
			} );
		} );
	}
}
