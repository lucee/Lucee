component extends="org.lucee.cfml.test.LuceeTestCase" {

	// every element records the value it starts with and then changes it. With 6 elements on 2 threads
	// at least 4 elements run on a reused clone, so any leaked setting shows up regardless of timing
	function run( testResults, testBox ) {
		describe( "LDEV-6906 parallel each/map does not leak request settings between elements", function() {

			it( "setTimezone in one element does not affect other elements", function() {
				var origTZ = getTimezone();
				setTimezone( "UTC" );
				try {
					var res = [ 1, 2, 3, 4, 5, 6 ].map( function( v, i ) {
						var seen = getTimezone();
						setTimezone( "Asia/Tokyo" );
						return seen;
					}, true, 2 );
				}
				finally {
					setTimezone( origTZ );
				}

				expect( res ).toBe( [ "UTC", "UTC", "UTC", "UTC", "UTC", "UTC" ] );
			});

			it( "setting requesttimeout in one element does not affect other elements", function() {
				var timeout = getPageContext().getRequestTimeout();
				var res = [ 1, 2, 3, 4, 5, 6 ].map( function( v, i ) {
					var seen = getPageContext().getRequestTimeout();
					setting requesttimeout=1234;
					return seen;
				}, true, 2 );

				expect( res ).toBe( [ timeout, timeout, timeout, timeout, timeout, timeout ] );
			});

			it( "unbalanced enablecfoutputonly in one element does not affect other elements", function() {
				var res = [ 1, 2, 3, 4, 5, 6 ].map( function( v, i ) {
					var seen = getPageContext().getCFOutputOnly();
					setting enablecfoutputonly=true;
					return seen;
				}, true, 2 );

				expect( res ).toBe( [ 0, 0, 0, 0, 0, 0 ] );
			});

			it( "switching the application in one element does not affect other elements", function() {
				var appName = getApplicationMetadata().name;
				var res = [ 1, 2, 3, 4, 5, 6 ].map( function( v, i ) {
					var seen = getApplicationMetadata().name;
					application name="LDEV6906_other_#createUUID()#";
					return seen;
				}, true, 2 );

				expect( res ).toBe( [ appName, appName, appName, appName, appName, appName ] );
			});

		});
	}
}
