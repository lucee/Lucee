component extends = "org.lucee.cfml.test.LuceeTestCase" labels="mssql" {

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-1680", function() {
			it(title="Checking the MSSQL datetimeoffset field value", skip="#notHasMssql()#", body=function( currentSpec ) {
				var result = _internalRequest(
					template = "#createURI("LDEV1680")#/LDEV1680.cfm"
				)
				var arr = listToArray(result.filecontent,"|");
				expect(arr[1].trim()).toBetrue();
				expect(arr[2].trim()).toBe("01/01/2022 10:10:10");
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