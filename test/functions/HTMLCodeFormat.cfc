component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe(title="Testcase for htmlCodeFormat() function", body=function() {
			it(title="Checking the htmlCodeFormat() function", body=function( currentSpec ) {
				var testString = "<This text is inside of angle brackets> This text is outside of angle brackets !!!";
				expect(htmlCodeFormat(testString)).toBe("<PRE>&lt;This text is inside of angle brackets&gt; This text is outside of angle brackets !!!</PRE>");
			});
		});
	}
}
