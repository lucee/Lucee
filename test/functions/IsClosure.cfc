component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for isClosure() function", body=function() {
			it(title="Checking the isClosure() function", body=function( currentSpec ) {
				var closureFunc = function(){
					return true;
				};
				function testUdf(){
					return true;
				}
				expect(isClosure(closureFunc)).toBeTrue();
				expect(isClosure(testUdf)).toBeFalse();
			});
		});
	}
}