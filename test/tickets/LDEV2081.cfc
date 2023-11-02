component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-2081", body=function() {
			it( title='checking DateFormat function with mask "W(week of the month)" ',body=function( currentSpec ) {
				assertEquals("3-September", dateFormat("09/14/2018", "W-mmmm"));
				assertEquals("3", dateFormat("september/14/2018", "W"));
				assertEquals("4/4", dateFormat("04/24/2018", "W/m"));
				assertEquals("5@2018", dateFormat("07/31/2018", "W@yyyy"));
				assertEquals("1//1900", dateFormat("01/01/1900", "W//yyyy"));
			});

			it( title='checking DateFormat function with mask "WW(week of the month with leading Zero)" ',body=function( currentSpec ) {
				assertEquals("03-2018", dateFormat("09/14/2018", "WW-yyyy"));
				assertEquals("03", dateFormat("september/14/2018", "WW"));
				assertEquals("04/2018", dateFormat("03/22/2018", "WW/yyyy"));
				assertEquals("05@July", dateFormat("07/31/2018", "WW@mmmm"));
				assertEquals("02//01", dateFormat("01/11/2018", "WW//mm"));
			});

			it( title='checking DateFormat function with mask "w(week of the year)" ',body=function( currentSpec ) {
				assertEquals("37-September", dateFormat("09/14/2018", "w-mmmm"));
				assertEquals("37", dateFormat("september/14/2018", "w"));
				assertEquals("17/4", dateFormat("04/24/2018", "w/m"));
				assertEquals("31@2018", dateFormat("07/31/2018", "w@yyyy"));
				assertEquals("1//1900", dateFormat("01/01/1900", "w//yyyy"));
			});

			it( title='checking DateFormat function with mask "ww(week of the year)" ',body=function( currentSpec ) {
				assertEquals("37-2018", dateFormat("09/14/2018", "ww-yyyy"));
				assertEquals("37", dateFormat("september/14/2018", "ww"));
				assertEquals("12/2018", dateFormat("03/22/2018", "ww/yyyy"));
				assertEquals("31@July", dateFormat("07/31/2018", "ww@mmmm"));
				assertEquals("02//01", dateFormat("01/11/2018", "ww//mm"));
			});
		});
	}
}
