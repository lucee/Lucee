component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function beforeAll() {
		variables.uri = createURI("LDEV4425");
	}

	function afterAll() {
		if (!notHasMssql()) {
			queryExecute( sql="DROP TABLE IF EXISTS LDEV4425", options: {
				datasource: server.getDatasource("mssql")
			}); 
		}
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4425", function() {
			it( title="Check generatedKey in insert operation using queryExecute() with returntype=query", skip="#notHasMssql()#", body=function( currentSpec ){
				try {
					var result = _internalRequest(
						template = "#variables.uri#/test4425.cfm",
						forms = {scene:1}
					).fileContent;
				}
				catch(any e) {
					result = e.message;
				}
				expect(trim(result)).toBeTrue();
			});

			it( title="Check generatedKey in insert operation using queryExecute() with returntype=array", skip="#notHasMssql()#", body=function( currentSpec ){
				try {
					var result = _internalRequest(
						template = "#variables.uri#/test4425.cfm",
						forms = {scene:2}
					).fileContent;
				}
				catch(any e) {
					result = e.message;
				}
				expect(trim(result)).toBeTrue();
			});

			it( title="Check generatedKey in insert operation using queryExecute() with returntype=struct", skip="#notHasMssql()#", body=function( currentSpec ){
				try {
					var result = _internalRequest(
						template = "#variables.uri#/test4425.cfm",
						forms = {scene:3}
					).fileContent;
				}
				catch(any e) {
					result = e.message;
				}
				expect(trim(result)).toBeTrue();
			});

		});
	}

	private function notHasMssql() {
		return structCount(server.getDatasource("mssql")) == 0;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
