component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for lsDayOfWeek()", body=function() {
			it(title="checking lsDayOfWeek() function for sunday", body = function( currentSpec ) {
				//UTC noon is the same day in the US and CH
				var date=createDateTime(2022,11,20,12,0,0,0,"UTC"); // a sunday
				// in the US Sunday is the first day of he week
				expect(lsDayOfWeek(date,"EN_US","America/Los_Angeles")).toBe(1);
				// in CH Sunday is the last day of he week
				expect(lsDayOfWeek(date,"EN_US","America/Los_Angeles")).toBe(7);
			});
			it(title="checking lsDayOfWeek() function for monday", body = function( currentSpec ) {
				//UTC noon is the same day in the US and CH
				var date=createDateTime(2022,11,21,12,0,0,0,"UTC"); // a monday
				// in the US Monday is the second day of he week
				expect(lsDayOfWeek(date,"EN_US","America/Los_Angeles")).toBe(2);
				// in CH Monday is the first day of he week
				expect(lsDayOfWeek(date,"EN_US","America/Los_Angeles")).toBe(1);
			});
		});
	}
}