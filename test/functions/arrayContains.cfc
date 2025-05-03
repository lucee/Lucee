component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Testcase for arrayContains() function", body = function() {
			it(title = "checking arrayContains() function", body = function( currentSpec ) {
				var arr = ["hello", "world"];
				expect(arrayContains(arr, "world")).toBeTrue();
				expect(arrayContains(arr, "WoRld")).toBeFalse();
				expect(arrayContains(["hello", " "], " ")).toBeTrue();
			});

			it(title = "checking array.contains() with member function", body = function( currentSpec ) {
				var arr = ["hello", "world", "Earth", " "];
				expect(arr.contains("world")).toBeTrue();
				expect(arr.contains(" ")).toBeTrue();
				expect(["hello", "world", "Earth"].contains("Earth")).toBeTrue();
			});
		});
	}
}
