component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6166 max:n structs must be safe for concurrent access", function() {

			it( "survives concurrent reads and writes and keeps its size limit", function() {
				var sct = structNew( "max:50" );
				var errors = [];
				var arr = [];
				arraySet( arr, 1, 16, 0 );
				arrayEach( arr, function( v, idx ) {
					try {
						for ( var i = 1; i <= 20000; i++ ) {
							var k = "k" & ( ( i * idx ) mod 120 );
							sct[ k ] = i;
							var tmp = sct[ k ] ?: "";
							if ( i mod 97 == 0 ) structDelete( sct, k );
						}
					}
					catch ( any e ) {
						arrayAppend( errors, e.message );
					}
				}, true, 16 );
				expect( errors ).toBeEmpty();
				expect( structCount( sct ) ).toBeLTE( 50 );
				// iterating must still work and see a consistent map
				var n = 0;
				loop collection=sct key="local.k" {
					n++;
				}
				expect( n ).toBe( structCount( sct ) );
			});
		});
	}
}
