component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for LDEV-3765", function() {
			it(title="Replace() with struct", body=function( currentSpec ) {
				expect(replace("one two three four", {"one":1, "two":2, "three":3, "four":4})).toBe("1 2 3 4");
				expect(replace("one two three", {"one":1, "two":2, "three":3})).toBe("1 2 3");
				expect(replace("one two three foo", {"one":1, "two":2, "three":3, "foo":4})).toBe("1 2 3 4");
				expect(replace("one two three", {"one":1, "two":2, "foo":3})).toBe("1 2 three"); // last element not matched
				expect(replace("one two three", {"one":1, "two":2, "three":3, "four":4})).toBe("1 2 3");
				expect(replace("one two three", {"five":1, "six":2 })).toBe("one two three"); // i.e no matches
			});
			it(title="ReplaceNoCase() with struct", body=function( currentSpec ) {
				expect(replaceNoCase("one two three four", {"ONE":1, "Two":2, "three":3, "four":4})).toBe("1 2 3 4");
				expect(replaceNoCase("one two three", {"ONE":1, "Two":2, "three":3})).toBe("1 2 3");
				expect(replaceNoCase("one two three foo", {"ONE":1, "Two":2, "three":3, "foo":4})).toBe("1 2 3 4");
				expect(replaceNoCase("one two three", {"ONE":1, "Two":2, "foo":3})).toBe("1 2 three"); // last element not matched
				expect(replaceNoCase("one two three", {"ONE":1, "Two":2, "three":3, "four":4})).toBe("1 2 3");
				expect(replaceNoCase("one two three", {"five":1, "six":2 } )).toBe("one two three"); // i.e no matches
			});
		});
	}
}
