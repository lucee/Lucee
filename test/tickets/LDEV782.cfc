component extends="org.lucee.cfml.test.LuceeTestCase" labels="scheduler" {

	function run( testResults, testBox ) {
		describe( "LDEV-782 scheduled task with an end time before the start time (window spanning midnight)", function() {

			beforeEach( function() {
				variables.util = createObject( "java", "lucee.commons.date.DateTimeUtil" ).getInstance();
				variables.tz = createObject( "java", "java.util.TimeZone" ).getTimeZone( "UTC" );
				variables.STT = createObject( "java", "lucee.runtime.schedule.ScheduledTaskThread" );
				variables.UNIX0 = createDateTime( 1970, 1, 1, 0, 0, 0, 0, "UTC" );
				// start 22:00, end 06:00, every 30 minutes
				variables.start = createDateTime( 2020, 11, 1, 22, 0, 0, 0, "UTC" ).getTime();
				variables.endTime = 6 * 3600000;
				variables.amount = 1800;
			});

			function next( now, notNow=true ) {
				var n = STT.calculateNextExecutionEvery( util, now.getTime(), arguments.notNow, tz, start, endTime, amount );
				return dateAdd( "l", n, UNIX0 );
			}

			it( "runs in the evening part of the window", function() {
				expect( next( createDateTime( 2021, 1, 15, 22, 10, 0, 0, "UTC" ) ) ).toBe( createDateTime( 2021, 1, 15, 22, 30, 0, 0, "UTC" ) );
			});

			it( "continues after midnight", function() {
				expect( next( createDateTime( 2021, 1, 15, 23, 45, 0, 0, "UTC" ) ) ).toBe( createDateTime( 2021, 1, 16, 0, 0, 0, 0, "UTC" ) );
			});

			it( "runs in the morning part of the window", function() {
				expect( next( createDateTime( 2021, 1, 16, 2, 10, 0, 0, "UTC" ) ) ).toBe( createDateTime( 2021, 1, 16, 2, 30, 0, 0, "UTC" ) );
			});

			it( "runs at the end time of the window", function() {
				expect( next( createDateTime( 2021, 1, 16, 5, 45, 0, 0, "UTC" ) ) ).toBe( createDateTime( 2021, 1, 16, 6, 0, 0, 0, "UTC" ) );
			});

			it( "does not run after the end time but waits for the start time", function() {
				expect( next( createDateTime( 2021, 1, 16, 6, 0, 0, 0, "UTC" ) ) ).toBe( createDateTime( 2021, 1, 16, 22, 0, 0, 0, "UTC" ) );
				expect( next( createDateTime( 2021, 1, 16, 12, 0, 0, 0, "UTC" ) ) ).toBe( createDateTime( 2021, 1, 16, 22, 0, 0, 0, "UTC" ) );
			});

			it( "starts at the start time when it is now", function() {
				expect( next( createDateTime( 2021, 1, 16, 22, 0, 0, 0, "UTC" ), false ) ).toBe( createDateTime( 2021, 1, 16, 22, 0, 0, 0, "UTC" ) );
			});
		});
	}
}
