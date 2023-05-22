component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for generate3DESKey() function", body=function() {
			it(title="Checking the generate3DESKey() function", body=function( currentSpec ) {
				expect(generate3DESKey('I love lucee')).toBe("SSBsb3ZlIGx1Y2Vl");
			});
		});
	}
}