component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for trimWhitespace() function", body=function() {
			it(title="Checking the trimWhitespace() function", body=function( currentSpec ) {
				var string = "     I love Lucee     ";
				expect(string.len()).toBe("22");
				expect(trimWhitespace(string).len()).toBe("14");
			});
			it(title="Checking the string.trimWhiteSpace() member function", body=function( currentSpec ) {
				var string = "     I love Lucee     ";
				expect(string.trimWhitespace().len()).toBe("14");
			});
		});
	}
}