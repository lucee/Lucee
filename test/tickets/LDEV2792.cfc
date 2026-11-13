component extends="org.lucee.cfml.test.LuceeTestCase" labels="function" {

	function run( testResults, testBox ) {
		describe( "LDEV-2792 closure with a name attribute", function() {

			it( title="a closure can be given a name with the name attribute", body=function() {
				var fn = function ( a, b ) name="comparator" {
					return a - b;
				};
				expect( fn( 5, 2 ) ).toBe( 3 );
			});

			it( title="a named closure can be used as array sort callback", body=function() {
				var arr = [ 3, 1, 2 ];
				arraySort( arr, function ( a, b ) name="comparator" {
					return a - b;
				});
				expect( arr ).toBe( [ 1, 2, 3 ] );
			});

			it( title="the closure name is used in the error message", body=function() {
				var fn = function ( x required array ) name="myNamedClosure" {};
				var msg = "";
				try {
					fn( true );
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toInclude( "myNamedClosure" );
			});

		});
	}

}
