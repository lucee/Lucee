component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4103", function() {
			it( title="Deserialize the json with large number value", body=function( currentSpec ) {
				var sampleJson = '{"LargeNumber":637944301333728800}';
				var res = DeserializeJSON(sampleJson);
				expect(res.LargeNumber).toBe("637944301333728800");
			});
		});
	}
}