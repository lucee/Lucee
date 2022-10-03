component extends = "org.lucee.cfml.test.LuceeTestCase" labels="mssql" {

	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-3559", function() {
			it(title="Checking decimal datatype values in cfquery", skip="#notHasMssql()#", body=function( currentSpec ) {
				var result = _internalRequest(
					template = "#createURI("LDEV3559")#/LDEV3559.cfm"
				).filecontent;
				expect(trim(result)).toBe("1.0,1.0");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function notHasMssql() {
		return structCount(server.getDatasource("mssql")) == 0;
	}
} 