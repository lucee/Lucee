component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for Beat()", body=function() {
			it(title="Checking Beat() function", body = function( currentSpec ) {
				assertEquals("500", beat(createDateTime(2000,1,1,12,0,0,0,"CET")));
				assertEquals("true", beat() GTE 0);
				assertEquals("541.666", beat(parseDateTime('01/01/2001 12:00:00+0')));
				assertEquals("500", beat(parseDateTime('01/01/2001 12:00:00+1')));
				assertEquals("500", beat(parseDateTime('30/06/2001 12:00:00+1')));
				assertEquals("458.333", beat(parseDateTime('01/01/2001 12:00:00+2')));
				assertEquals("416.666", beat(parseDateTime('01/01/2001 12:00:00+3')));
				assertEquals("375", beat(parseDateTime('01/01/2001 12:00:00+4')));
			});
		});
	}
}
