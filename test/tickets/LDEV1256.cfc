component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1256", function() {
			it( title='Checking parseDateTime() for timezones greater than 12 hours', body=function( currentSpec ) {

				expect(
					parseDateTime("2017-06-11T14:45:54+14:00", "yyyy-MM-dd'T'HH:mm:ssX")
				).toBe("2017-06-11T00:45:54Z");

				expect(
					parseDateTime("2017-06-11T00:45:54-13:00", "yyyy-MM-dd'T'HH:mm:ssX")
				).toBe("2017-06-11T13:45:54Z");
			});
		});
	}
}