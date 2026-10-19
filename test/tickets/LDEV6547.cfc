component extends="org.lucee.cfml.test.LuceeTestCase" labels="datetime" {

	function run( testResults, testBox ) {
		describe( "LDEV-6547 dateFormat/timeFormat with an X mask must not change the JVM default timezone", function() {

			it( "X / XX / XXX output in different timezones", function() {
				var winter = createDateTime( 2026, 1, 15, 12, 0, 0, 0, "UTC" );
				var summer = createDateTime( 2026, 7, 15, 12, 0, 0, 0, "UTC" );
				var cases = [
					{ tz: "UTC", d: winter, x: "Z", xx: "Z", xxx: "Z" },
					{ tz: "Europe/Zurich", d: winter, x: "+01", xx: "+0100", xxx: "+01:00" },
					{ tz: "Europe/Zurich", d: summer, x: "+02", xx: "+0200", xxx: "+02:00" },
					{ tz: "America/New_York", d: winter, x: "-05", xx: "-0500", xxx: "-05:00" },
					{ tz: "Asia/Kolkata", d: winter, x: "+05", xx: "+0530", xxx: "+05:30" }
				];
				for ( var c in cases ) {
					expect( timeFormat( c.d, "X", c.tz ) ).toBe( c.x, c.tz & " timeFormat X" );
					expect( timeFormat( c.d, "XX", c.tz ) ).toBe( c.xx, c.tz & " timeFormat XX" );
					expect( timeFormat( c.d, "XXX", c.tz ) ).toBe( c.xxx, c.tz & " timeFormat XXX" );
					expect( dateFormat( c.d, "X", c.tz ) ).toBe( c.x, c.tz & " dateFormat X" );
					expect( dateFormat( c.d, "XX", c.tz ) ).toBe( c.xx, c.tz & " dateFormat XX" );
					expect( dateFormat( c.d, "XXX", c.tz ) ).toBe( c.xxx, c.tz & " dateFormat XXX" );
				}
			});

			it( "JVM default timezone stays the same during and after concurrent X formats", function() {
				var TZ = createObject( "java", "java.util.TimeZone" );
				var before = TZ.getDefault().getID();
				var names = [];
				for ( var i = 1; i <= 4; i++ ) {
					var n = "ldev6547_#i#_#createUUID()#";
					arrayAppend( names, n );
					thread name=n action="run" iterations=20000 {
						var d = now();
						for ( var j = 1; j <= attributes.iterations; j++ ) {
							timeFormat( d, "HH:mm:ss X" );
							dateFormat( d, "yyyy-mm-dd XXX" );
						}
					}
				}
				var seen = {};
				var running = true;
				while ( running ) {
					seen[ TZ.getDefault().getID() ] = true;
					running = false;
					for ( var n in names ) {
						if ( listFindNoCase( "NOT_STARTED,RUNNING", cfthread[ n ].status ) ) {
							running = true;
							break;
						}
					}
				}
				thread action="join" name=arrayToList( names );
				for ( var n in names ) {
					expect( cfthread[ n ].status ).toBe( "COMPLETED", cfthread[ n ].error.message ?: "" );
				}
				expect( structKeyList( seen ) ).toBe( before );
				expect( TZ.getDefault().getID() ).toBe( before );
			});

		});
	}
}
