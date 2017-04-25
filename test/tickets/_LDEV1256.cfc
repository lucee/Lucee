component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1256", function() {
			it( title='Checking parseDateTime() for timezones greater than 12 hours', body=function( currentSpec ) {
			 	var dateTimestring = "2017-03-03T19:20:30+13:00";
				expect( parseDateTime(dateTimestring, "yyyy-MM-dd'T'HH:mm:ssX") ).toBe(createDateTime(2018,08,03,11,30,30));
			});
		});
	}
}