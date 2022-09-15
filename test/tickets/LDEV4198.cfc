component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function beforeAll() {
		variables.tz = getTimeZone();
		setTimeZone("UTC");
	}

	function afterAll() {
		setTimeZone(variables.tz);
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV4198", function() {
			it( title="Checking dateFormat() with mask 'ISO'", body=function( currentSpec ) {
				expect(dateFormat("2022/09/03 11:45:05.666", "iso")).toBe("2022-09-03");
				expect(dateFormat("2022/09/03", "iso")).toBe("2022-09-03");
			});
		});
	}

}