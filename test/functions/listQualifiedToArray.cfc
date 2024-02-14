component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( "Testcase for listQualifiedToArray()", function() {
			it(title = "Checking with listQualifiedToArray()", body = function( currentSpec ) {
				var list = "I,love,lucee";
				arr = listQualifiedToArray(list);
				expect(arr[1]).toBe("I");
				expect(arr.len()).toBe(3);
			});
			it(title = "Checking with listQualifiedToArray()", body = function( currentSpec ) {
				var list = "I'love,lucee";
				arr = listQualifiedToArray(list, "'");
				expect(arr[1]).toBe("I");
				expect(arr[2]).toBe("love,lucee");
				expect(arr.len()).toBe(2);
			});
		});
	}
}