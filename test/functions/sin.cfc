component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for sin() function", body=function() {
			it(title="Checking the sin() function", body=function( currentSpec ) {
				var a = 90;
				expect(sin(a)).toBe(0.8939966636005579);
			});0.893996663601

			it(title="Checking the sin() member function", body=function( currentSpec ) {
				var a = 90;
				expect(a.sin()).toBe(0.8939966636005579);
			});

			it(title="Checking the sin() function result is numeric", body=function( currentSpec ) {
				var a = 90;
				expect(isnumeric(sin(a))).toBeTrue();
			});

			it(title="Checking the sin() function to string", body=function( currentSpec ) {
				var a = 90;
				assertEquals("0.8939966636005579","#tostring(sin(a))#");
			});
		});
	}
}