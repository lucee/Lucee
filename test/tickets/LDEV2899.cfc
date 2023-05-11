component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function run( testResults, textbox ) {
		describe("Testcase for LDEV-2899", function() {
			it(title="Checking booleanFormat() with nested structure", body=function( currentSpec ) {
				var a = { b: { c: true } };
				try {
					var result = booleanFormat(a?.c?.d);
				}
				catch(any e) {
					var result = e.message;
				}
				expect(result).toBe("false");
			});
		});
	}
}
