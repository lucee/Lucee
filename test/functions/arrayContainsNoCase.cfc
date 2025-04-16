component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Testcase for arrayContainsNoCase() function", body = function() {
			it(title = "checking arrayContainsNoCase() function", body = function( currentSpec ) {
				var arr = ["hello", "world"];
				expect(arrayContainsNoCase(arr, "World")).toBeTrue();
				expect(arrayContainsNoCase(arr, "WORLD")).toBeTrue();
				expect(arrayContainsNoCase(["hello", "world"], "World")).toBeTrue();
				expect(arrayContainsNoCase(["hello", "world"], "WORLD")).toBeTrue();
			});

			it(title = "checking arrayContainsNoCase() with memberFunction", body = function( currentSpec ) {
				var arr = ["hello", "world"];
				expect(arr.containsNoCase("worLD")).toBeTrue();
				expect(arr.containsNoCase("WORLD")).toBeTrue();
				expect( ["hello", "world"].containsNoCase("worLD")).toBeTrue();
				expect( ["hello", "world"].containsNoCase("WORLD")).toBeTrue();
			});
		});
	}
}
