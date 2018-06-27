component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for DayOfWeekAsString()", body=function() {
			it(title="checking DayOfWeekAsString() function", body = function( currentSpec ) {
				var org=getLocale();
				setLocale("German (Swiss)");
				assertEquals("Sonntag","#DayOfWeekAsString(1)#");
				assertEquals("Montag","#DayOfWeekAsString(2)#");
				assertEquals("Dienstag","#DayOfWeekAsString(3)#");
				assertEquals("Mittwoch","#DayOfWeekAsString(4)#");
				assertEquals("Donnerstag","#DayOfWeekAsString(5)#");
				assertEquals("Freitag","#DayOfWeekAsString(6)#");
				assertEquals("Samstag","#DayOfWeekAsString(7)#");

				try {
			        assertEquals("Invalid DayOfWeek", "#DayOfWeekAsString(0)#");
			        fail("must throw:0.0 must be within range: ( 1 : 7 ) ");
				} catch (any e){}
				       
				try {
			        assertEquals("Invalid DayOfWeek", "#DayOfWeekAsString(8)#");
			        fail("must throw:0.0 must be within range: ( 1 : 7 ) ");
				} catch (any e){}

				setLocale("English (US)");
				assertEquals("Sunday", "#DayOfWeekAsString(1)#");
				assertEquals("Monday", "#DayOfWeekAsString(2)#");
				assertEquals("Tuesday", "#DayOfWeekAsString(3)#");
				assertEquals("Wednesday", "#DayOfWeekAsString(4)#");
				assertEquals("Thursday", "#DayOfWeekAsString(5)#");
				assertEquals("Friday", "#DayOfWeekAsString(6)#");
				assertEquals("Saturday", "#DayOfWeekAsString(7)#");

				try {
				    valueEquals(left="#DayOfWeekAsString(0)#", right="Invalid DayOfWeek");
				    fail("must throw:0.0 must be within range: ( 1 : 7 ) ");
				} catch (any e){}

				try {
				    valueEquals(left="#DayOfWeekAsString(8)#", right="Invalid DayOfWeek");
				    fail("must throw:0.0 must be within range: ( 1 : 7 ) ");
				} catch (any e){}
			});
		});
	}
}
