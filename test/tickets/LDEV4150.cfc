component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" skip="true" {
	
	function beforeAll() {
		variables.uri = createURI("LDEV4150");
	}
	
	function afterAll() {
		if (!notHasMssql()) {
			queryExecute( sql="DROP TABLE IF EXISTS test4150", options: {
				datasource: server.getDatasource("mssql")
			}); 
		}
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4150",  function() {
			it( title="checking length property value to sqltype=varchar on ORM Entity", skip="#notHasMssql()#",  body=function( currentSpec ) {
				local.result = _InternalRequest(
						template : "#uri#\LDEV4150.cfm"
				).filecontent;
				expect(trim(result)).tobe("Success");
			});
		});
	}

	private function notHasMssql() {
		return structCount(server.getDatasource("mssql")) == 0;
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}