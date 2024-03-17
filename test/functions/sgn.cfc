component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for sgn() function", body=function() {
			it(title="Checking the sgn() function", body=function( currentSpec ) {
				 a =7;
				 b =-7;
				 expect(sgn(a)).toBe(1);
				 expect(sgn(b)).toBe(-1);
				 expect(a.sgn()).toBe(1);
				 expect(b.sgn()).toBe(-1);
				 expect(isnumeric(sgn(a))).toBeTrue();
			});
		});
	}
}