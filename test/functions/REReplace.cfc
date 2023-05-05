component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title="Testcase for reReplace", body=function() {
			it(title="Checking with reReplace function", body=function( currentSpec ) {
				assertEquals("xxdefxxabcxx", reReplace("xxabcxxabcxx", "abc", "def"));
				assertEquals("xxdefxxdefxx", reReplace("xxabcxxabcxx", "abc", "def", "all"));
				assertEquals("xxdefxxabcxx", reReplace("xxABCxxabcxx", "ABC", "def", "all"));
				assertEquals("GABARET", reReplace("CABARET", "C|B", "G"));
				assertEquals("CAGARET", reReplace("CABARET", "c|B", "G"));
				assertEquals("GAGARET", reReplace("CABARET", "C|B", "G", "ALL"));
			});

			it(title="Checking with string.reReplace() member function", body=function( currentSpec ) {
				assertEquals("xxdefxxabcxx", "xxabcxxabcxx".reReplace("abc", "def"));
				assertEquals("xxdefxxdefxx", "xxabcxxabcxx".reReplace("abc", "def", "all"));
				assertEquals("xxdefxxabcxx", "xxABCxxabcxx".reReplace("ABC", "def", "all"));
				assertEquals("GABARET", "CABARET".reReplace("C|B", "G"));
				assertEquals("CAGARET", "CABARET".reReplace("c|B", "G"));
				assertEquals("GAGARET", "CABARET".reReplace("C|B", "G", "ALL"));
			});
		});
	}
}