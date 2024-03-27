component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for stripCr() function", body=function() {
			it(title="Checking the stripCr() function", body=function( currentSpec ) {
				var string = "I love lucee"&chr(13);
				var result = stripCr(string);
				expect(len(string)).toBe("13");
				expect(len(result)).toBe("12");
			});
		});
	}
}