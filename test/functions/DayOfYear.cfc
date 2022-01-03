component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for DayOfYear()", body=function() {
			it(title="checking DayOfYear() function", body = function( currentSpec ) {
				d1 = CreateDateTime(2001, 12, 1, 4, 10, 1);
				assertEquals("335", "#dayOfYear(d1)#");
				assertEquals("#DayOfYear(1)#", "365");
			});

			it(title="checking DateTime.DayOfYear() member function", body=function( currentSpec ) {
				d1 = CreateDateTime(2001, 12, 1, 4, 10, 1);
				assertEquals("335", "#d1.dayOfYear()#");
			});
		});
	}
}