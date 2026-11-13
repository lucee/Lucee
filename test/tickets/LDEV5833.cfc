component extends="org.lucee.cfml.test.LuceeTestCase" labels="datetime" {

	function run( testResults, testBox ) {
		var date1 = createDateTime( year=2025, month=9, day=25, hour=12, minute=30, second=30, millisecond=555 );
		var date2 = createDateTime( year=2025, month=9, day=25, hour=12, minute=30, second=30, millisecond=7 );
		var date3 = createDateTime( year=2025, month=9, day=25, hour=12, minute=30, second=30, millisecond=90 );

		describe( "LDEV-5833 dateTimeFormat() mask l (milliseconds)", function() {

			it( title="single 'l' mask, milliseconds without leading zeros", body=function( currentSpec ) {
				expect( dateTimeFormat( date1, "l" ) ).toBe( "555" );
				expect( dateTimeFormat( date2, "l" ) ).toBe( "7" );
				expect( dateTimeFormat( date3, "l" ) ).toBe( "90" );
				expect( dateTimeFormat( date2, "L" ) ).toBe( "7" );
			});

			it( title="double 'll' mask, at least 2 digits", body=function( currentSpec ) {
				expect( dateTimeFormat( date1, "ll" ) ).toBe( "555" );
				expect( dateTimeFormat( date2, "ll" ) ).toBe( "07" );
				expect( dateTimeFormat( date3, "ll" ) ).toBe( "90" );
			});

			it( title="triple 'lll' mask, 3 digits", body=function( currentSpec ) {
				expect( dateTimeFormat( date1, "lll" ) ).toBe( "555" );
				expect( dateTimeFormat( date2, "lll" ) ).toBe( "007" );
				expect( dateTimeFormat( date3, "lll" ) ).toBe( "090" );
			});

			it( title="'l' inside a longer mask", body=function( currentSpec ) {
				expect( dateTimeFormat( date2, "HH:nn:ss.l" ) ).toBe( "12:30:30.7" );
				expect( dateTimeFormat( date2, "yyyy-mm-dd HH:nn:ss.lll" ) ).toBe( "2025-09-25 12:30:30.007" );
				expect( dateTimeFormat( date1, "l 'l'" ) ).toBe( "555 l" );
			});

			it( title="string and parseDateTime input (ticket example)", body=function( currentSpec ) {
				expect( dateTimeFormat( "2025-09-24 14:37:12.123", "l" ) ).toBe( "123" );
				expect( dateTimeFormat( parseDateTime( "2025-09-24 14:37:12.123" ), "l" ) ).toBe( "123" );
			});
		});
	}
}
