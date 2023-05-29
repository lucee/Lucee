component extends = "org.lucee.cfml.test.LuceeTestCase" labels="mssql" skip="true" {

	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-4337", function() {
			it(title="Checking cfqueryparam with scale attribute", skip="#notHasMssql()#", body=function( currentSpec ) {
				var result = _internalRequest(
					template = "#createURI("LDEV4337")#/LDEV4337.cfm"
				).filecontent;

				expect(trim(result)).toBe("27.1800");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function notHasMssql() {
		return structCount(server.getDatasource("mssql")) == 0;
	}
} 
