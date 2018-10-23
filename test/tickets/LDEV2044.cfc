component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2044", function() {
			it(title = "Diff member function for dateDiff incompatible with ACF", body = function( currentSpec ) {
				var currDate= now();
				var nextDate = dateAdd('d', 2, currDate);
				expect(currDate.diff('d', nextDate)).toBe(-2);
			});
		});
	}
}