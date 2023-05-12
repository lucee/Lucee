component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe(title="Testcase for isWddx() function", body=function() {
			it(title="Checking the isWddx() function", body=function( currentSpec ) {
				cfwddx(action="CFML2WDDX", input="I love lucee", output="MyWDDXPacket");
				expect(isWddx(MyWDDXPacket)).toBeTrue();
				expect(isWddx("I love lucee")).toBeFalse();
			});
		});
	}
}