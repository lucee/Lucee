component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function beforeAll() {
		if (!notHasMysql()) {
			queryExecute( sql="DROP TABLE IF EXISTS ldev5541", options: {
				datasource: server.getDatasource("mysql")
			});
		}
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-5541",  function() {

			it( title="checking orm doesn't cast 51-1 to date", skip="#notHasMysql()#",  body=function( currentSpec ) {
				var uri = createURI("LDEV5541");
				var result = _InternalRequest(
						template = "#uri#/LDEV5541.cfm",
						forms = {scene:1}
				).filecontent.trim();
				expect(result.toJson()).tobe('"51-1"');
			});
		});
	}

	private function notHasMysql() {
		if ( structCount( server.getTestService("orm") ) eq 0 ) return true;
		return structCount(server.getDatasource("mysql")) == 0;
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
