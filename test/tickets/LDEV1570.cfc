component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(  testResults , testBox ) {
		describe( title="Test suite for LDEV-1570",  body=function() {
			it(title="checking Mask in dateTimeFormat()", body = function( currentSpec ) {
				time = "{ts '2017-11-01 13:35:08'}";
				assertEquals("2017-11-01T13:11:08-05:00" , dateTimeFormat(time, "yyyy-MM-dd'T'HH:mm:ssXXX", "America/Chicago"));
				assertEquals("2017-11-01T13:11:08-0500" , dateTimeFormat(time, "yyyy-MM-dd'T'HH:mm:ssZZZ", "America/Chicago"));
				assertEquals("2017-11-01T13:35:08+0530" , dateTimeFormat(time, "ISO8601"));
			});
		});
	}
}
