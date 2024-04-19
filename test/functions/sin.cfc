component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for sin() function", body=function() {
			it(title="Checking the sin() function", body=function( currentSpec ) {
				a = 90;
				expect(sin(a)).toBe(0.8939966636005579);
				expect(a.sin()).toBe(0.8939966636005579);
				expect(isnumeric(sin(a))).toBeTrue();
				assertEquals("0.893996663601","#tostring(sin(a))#");
			});
		});
	}
}