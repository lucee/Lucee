component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6906 parallel each/map does not leak request settings between elements", function() {

			it( "setTimezone in one element does not affect other elements", function() {
				var origTZ = getTimezone();
				setTimezone( "UTC" );
				var res = [ 1, 2, 3, 4, 5, 6 ].map( function( v, i ) {
					if ( i == 1 ) setTimezone( "Asia/Tokyo" );
					sleep( i == 2 ? 600 : 100 ); // element 2 keeps its thread busy, so elements 3-6 reuse the clone of element 1
					return getTimezone();
				}, true, 2 );
				setTimezone( origTZ );

				expect( res ).toBe( [ "Asia/Tokyo", "UTC", "UTC", "UTC", "UTC", "UTC" ] );
			});

			it( "setting requesttimeout in one element does not affect other elements", function() {
				var timeout = getPageContext().getRequestTimeout();
				var res = [ 1, 2, 3, 4, 5, 6 ].map( function( v, i ) {
					if ( i == 1 ) setting requesttimeout=1234;
					sleep( i == 2 ? 600 : 100 );
					return getPageContext().getRequestTimeout();
				}, true, 2 );

				expect( res ).toBe( [ 1234000, timeout, timeout, timeout, timeout, timeout ] );
			});

		});
	}
}
