component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for DaysInMonth()", body=function() {
			it(title="checking DaysInMonth() function", body=function( currentSpec ) {
				d1 = CreateDateTime(2001, 12, 1, 4, 10, 1);
				assertEquals("31", "#daysInMonth(d1)#");
					
				<!--- all month of a year --->
				assertEquals("31", "#DaysInMonth(CreateDateTime(2001,  1, 1, 0, 0, 0))#");
				assertEquals("28", "#DaysInMonth(CreateDateTime(2001,  2, 1, 0, 0, 0))#");
				assertEquals("31", "#DaysInMonth(CreateDateTime(2001,  3, 1, 0, 0, 0))#");
				assertEquals("30", "#DaysInMonth(CreateDateTime(2001,  4, 1, 0, 0, 0))#");
				assertEquals("31", "#DaysInMonth(CreateDateTime(2001,  5, 1, 0, 0, 0))#");
				assertEquals("30", "#DaysInMonth(CreateDateTime(2001,  6, 1, 0, 0, 0))#");
				assertEquals("31", "#DaysInMonth(CreateDateTime(2001,  7, 1, 0, 0, 0))#");
				assertEquals("31", "#DaysInMonth(CreateDateTime(2001,  8, 1, 0, 0, 0))#");
				assertEquals("30", "#DaysInMonth(CreateDateTime(2001,  9, 1, 0, 0, 0))#");
				assertEquals("31", "#DaysInMonth(CreateDateTime(2001, 10, 1, 0, 0, 0))#");
				assertEquals("30", "#DaysInMonth(CreateDateTime(2001, 11, 1, 0, 0, 0))#");
				assertEquals("31", "#DaysInMonth(CreateDateTime(2001, 12, 1, 0, 0, 0))#");

				<!--- leap year --->
				assertEquals("29", "#DaysInMonth(CreateDateTime(2004, 2, 1, 0, 0, 0))#");
				assertEquals("29", "#DaysInMonth(CreateDateTime(2000, 2, 1, 0, 0, 0))#");
				assertEquals("28", "#DaysInMonth(CreateDateTime(1900, 2, 1, 0, 0, 0))#");

				<!--- numeric date --->
				assertEquals("31", "#DaysInMonth(1)#");
			});

			it(title="checking DateTime.DaysInMonth() member function", body=function( currentSpec ) {
				d1 = CreateDateTime(2001, 12, 1, 4, 10, 1);
				assertEquals("31", "#d1.daysInMonth()#");
					
				<!--- all month of a year --->
				assertEquals("31", "#CreateDateTime(2001,  1, 1, 0, 0, 0).daysInMonth()#");
				assertEquals("28", "#CreateDateTime(2001,  2, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("31", "#CreateDateTime(2001,  3, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("30", "#CreateDateTime(2001,  4, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("31", "#CreateDateTime(2001,  5, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("30", "#CreateDateTime(2001,  6, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("31", "#CreateDateTime(2001,  7, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("31", "#CreateDateTime(2001,  8, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("30", "#CreateDateTime(2001,  9, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("31", "#CreateDateTime(2001, 10, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("30", "#CreateDateTime(2001, 11, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("31", "#CreateDateTime(2001, 12, 1, 0, 0, 0).DaysInMonth()#");

				<!--- leap year --->
				assertEquals("29", "#CreateDateTime(2004, 2, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("29", "#CreateDateTime(2000, 2, 1, 0, 0, 0).DaysInMonth()#");
				assertEquals("28", "#CreateDateTime(1900, 2, 1, 0, 0, 0).DaysInMonth()#");

			});
		});
	}
}