component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults , testBox ) {
		describe( title="Testcase for fix() function", body=function() {
			it(title="Checking the fix() function", body=function( currentSpec ) {
				expect(fix(2.0)).toBe(2);
				expect(fix(7.2)).toBe(7);
				expect(fix(4.9)).toBe(4);
				expect(fix(-5.7)).toBe(-5);
			});
			it(title="Checking the fix() with member function", body=function( currentSpec ) {
				expect(10.112.fix()).toBe(10);
				expect(36.99.fix()).toBe(36);
				expect(-10.112.fix()).toBe(-10);
				expect(-6.98.fix()).toBe(-6);
			});
		});
	}
}
