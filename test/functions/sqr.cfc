component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for sqr() function", body=function() {
			it(title="Checking the sqr() function", body=function( currentSpec ) {
				var int = 64;
				expect(sqr(int)).toBe(8);
				expect(int.sqr()).toBe(8);
				expect(isnumeric(sqr(int))).toBeTrue();
				expect(sqr(4)).toBe(2);
			});
		});
	}
}
