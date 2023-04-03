component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for DaysInYear()", body=function() {
			it(title="checking DaysInYear() function", body=function( currentSpec ) {
				d1 = CreateDateTime(2001, 12, 1, 4, 10, 1);
				assertEquals("365", "#daysInYear(d1)#");
				assertEquals("#DaysInYear(1)#", "365" );
			});

			it(title="checking DateTime.DaysInYear() member function", body=function( currentSpec ) {
				d1 = CreateDateTime(2001, 12, 1, 4, 10, 1);
				assertEquals("365", "#d1.daysInYear()#");
			});
		});
	}
}