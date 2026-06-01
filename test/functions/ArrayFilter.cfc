component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayFilter() parallel modes", body=function() {

			var data = [ 1, 2, 3, 4, 5, 6 ];
			var isEven = function( value ){
				return value % 2 == 0;
			};
			// Filter preserves the input order, even when executed in parallel
			var expected = "2,4,6";

			it(title="filters sequentially when parallel is omitted", body = function( currentSpec ) {
				assertEquals( expected, arrayToList( arrayFilter( data, isEven ) ) );
			});

			it(title="filters with parallel='none'", body = function( currentSpec ) {
				assertEquals( expected, arrayToList( arrayFilter( data, isEven, "none" ) ) );
			});

			it(title="filters with parallel='thread'", body = function( currentSpec ) {
				assertEquals( expected, arrayToList( arrayFilter( data, isEven, "thread", 4 ) ) );
			});

			it(title="filters with parallel='virtual'", body = function( currentSpec ) {
				assertEquals( expected, arrayToList( arrayFilter( data, isEven, "virtual", 4 ) ) );
			});

			it(title="still accepts the deprecated boolean true/false", body = function( currentSpec ) {
				assertEquals( expected, arrayToList( arrayFilter( data, isEven, true, 4 ) ) );
				assertEquals( expected, arrayToList( arrayFilter( data, isEven, false ) ) );
			});

			it(title="throws on an invalid parallel value", body = function( currentSpec ) {
				var threw = false;
				try {
					arrayFilter( data, isEven, "bogus" );
				}
				catch ( any e ) {
					threw = true;
				}
				assertTrue( threw, "expected arrayFilter() with an invalid [parallel] value to throw" );
			});
		});
	}
}
