component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe(title="Testcase for DayOfWeekShortAsString()", body=function() {
			it(title="Checking the DayOfWeekShortAsString() function", body=function( currentSpec ) {
				var orgLocale = getLocale();
				setLocale("German (Swiss)");
				expect(DayOfWeekShortAsString(1)).toBe("So");
				expect(DayOfWeekShortAsString(2)).toBe("Mo");
				expect(DayOfWeekShortAsString(3)).toBe("Di");
				expect(DayOfWeekShortAsString(4)).toBe("Mi");
				expect(DayOfWeekShortAsString(5)).toBe("Do");
				expect(DayOfWeekShortAsString(6)).toBe("Fr");
				expect(DayOfWeekShortAsString(7)).toBe("Sa");

				setLocale("English (US)");
				expect(DayOfWeekShortAsString(1)).toBe("Sun");
				expect(DayOfWeekShortAsString(2)).toBe("Mon");
				expect(DayOfWeekShortAsString(3)).toBe("Tue");
				expect(DayOfWeekShortAsString(4)).toBe("Wed");
				expect(DayOfWeekShortAsString(5)).toBe("Thu");
				expect(DayOfWeekShortAsString(6)).toBe("Fri");
				expect(DayOfWeekShortAsString(7)).toBe("Sat");
				setLocale(orgLocale);
			});
		});
	}
}