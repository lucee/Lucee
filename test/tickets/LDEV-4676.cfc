component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title = "Testcase for LDEV-4676", body = function() {
			it( title = "Checking serializejson with CFC instance", body = function( currentSpec ) {
				var query = new Query();
				query.TEST = {};
				expect(serializeJSON(query)).toBe('{"TEST":{},"tagName":"query","params":[],"parts":[],"attributes":{}}');
			});
		});
	}
}
