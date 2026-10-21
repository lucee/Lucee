component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( "LDEV-6672: legacy three-letter time zone IDs are resolved lazily", function() {
			it( "short IDs still resolve", function() {
				var tzu = createObject( "java", "lucee.commons.date.TimeZoneUtil" );
				expect( tzu.toTimeZone( "PST" ).getID() ).toBe( "US/Pacific" );
				expect( tzu.toTimeZone( "EST" ).getID() ).toBe( "US/Eastern" );
				expect( tzu.toTimeZone( "SST" ).getID() ).toBe( "SST" );
				expect( tzu.toTimeZone( "America/New_York" ).getID() ).toBe( "America/New_York" );
				expect( tzu.toTimeZone( "Pacific Standard Time" ).getID() ).toBe( "US/Pacific" );
			});
		});
	}
}
