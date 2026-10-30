component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-3705 server start time in GetSystemMetrics", function() {

			it( title="exposes the server start time as a date", body=function() {
				var metrics = getSystemMetrics();
				expect( metrics ).toHaveKey( "startTime" );
				expect( isDate( metrics.startTime ) ).toBeTrue();
			});

			it( title="start time is in the past", body=function() {
				var metrics = getSystemMetrics();
				expect( dateDiff( "s", metrics.startTime, now() ) ).toBeGTE( 0 );
			});

			it( title="start time does not change between calls", body=function() {
				var first = getSystemMetrics().startTime;
				sleep( 50 );
				var second = getSystemMetrics().startTime;
				expect( dateCompare( first, second ) ).toBe( 0 );
			});

		});
	}

}
