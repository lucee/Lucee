component extends="org.lucee.cfml.test.LuceeTestCase" labels="datetime" {

	function beforeAll() {
		variables.timezone = getTimeZone();
	}

	function afterAll() {
		setTimeZone( variables.timezone );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6570 time values render with the hour they were created with", function() {

			it( "America/Vancouver and America/Edmonton", function() {
				for ( var tz in [ "America/Vancouver", "America/Edmonton" ] ) {
					setTimeZone( tz );
					var t = createTime( 8, 0, 0 );
					expect( timeFormat( t, "HH:mm:ss" ) ).toBe( "08:00:00", tz );
					expect( t.toString() ).toBe( "{t '08:00:00'}", tz );
					expect( toString( createODBCTime( "08:00" ) ) ).toBe( "{t '08:00:00'}", tz );
					expect( dateTimeFormat( t, "yyyy-mm-dd HH:nn:ss" ) ).toBe( "1899-12-30 08:00:00", tz );
				}
			});

			it( "createTime round trips in every timezone", function() {
				var failed = [];
				var ids = createObject( "java", "java.util.TimeZone" ).getAvailableIDs();
				for ( var tz in ids ) {
					setTimeZone( tz );
					var s = createTime( 8, 0, 0 ).toString();
					if ( s != "{t '08:00:00'}" ) arrayAppend( failed, tz & ": " & s );
				}
				expect( arrayToList( failed, "; " ) ).toBe( "" );
			});

		});
	}
}
