component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title="Testcase for replaceNocase", body=function() {
			it(title="Checking with replaceNoCase function", body=function( currentSpec ) {
				var text = "I Love Lucee";
				res=replaceNoCase(text, {"Lucee":"Lucee!"});

				expect(res).toBe("I Love Lucee!");
				expect("#replaceNoCase("xxabcxxabcxx", "abc", "def")#").toBe("xxdefxxabcxx");
				expect("#replaceNoCase("xxabcxxabcxx", "abc", "def", "All")#").toBe("xxdefxxdefxx");
				expect("#replaceNoCase("xxabcxxabcxx", "abc", "def", "hans")#").toBe("xxdefxxabcxx");
				expect("#replaceNoCase("abc", "a", "b", "all")#").toBe("bbc");
				expect("#replaceNoCase("a.b.c.d", ".", "-", "all")#").toBe("a-b-c-d");
			});

			it(title="Checking with string.replaceNoCase member function", body=function( currentSpec ) {
				expect("xxabcxxabcxx".ReplaceNocase("ABC", "def")).toBe("xxdefxxabcxx");
				expect("xxabcxxabcxx".ReplaceNocase("ABC", "def", "all")).toBe("xxdefxxdefxx");
				expect("xxabcxxabcxx".ReplaceNocase("AbC", "def", "all")).toBe("xxdefxxdefxx");
			});
		});
	}
}