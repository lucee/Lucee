component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(  testResults , testBox ) {
		describe( title="Test suite for LDEV-1570",  body=function() {
			it(title="checking Mask in dateTimeFormat()", body = function( currentSpec ) {
				var time = parseDateTime(date:"{ts '2017-11-01 13:35:08'}",timezone:"America/Chicago");
				assertEquals(
					"2017-11-01T13:35:08-05:00" ,
					dateTimeFormat(time, "yyyy-MM-dd'T'HH:nn:ssXXX", "America/Chicago"));
				assertEquals(
					"2017-11-01T13:35:08-0500" ,
					dateTimeFormat(time, "yyyy-MM-dd'T'HH:nn:ssZZZ", "America/Chicago"));
				assertEquals(
					"2017-11-01T13:35:08-05:00" ,
					dateTimeFormat(time, "ISO8601", "America/Chicago"));
				assertEquals(
					"2017-11-01T13:35:08-05:00" ,
					dateTimeFormat(time, "ISO", "America/Chicago"));
			});
		});
	}
}
