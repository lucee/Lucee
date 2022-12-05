component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4246", function() {

			it( title="calling static methods from an abstract component", body=function( currentSpec ) {
				try {
					var result = LDEV4246.abstract::testFunc();
				}
				catch(any e) {
					var result = e.message;
				}
				expect(trim(result)).toBe("static methods from an abstract component");
			});
		});
	}

}