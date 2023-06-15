component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" skip="true"{
	
	function beforeAll() {
		variables.uri = createURI("LDEV4137");
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4137",  function() labels="query" {
			it( title="checking large number value in query without using the cfqueryparam", skip="#notHasMSSQL()#",  body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV4137.cfm",
					forms : { scene = 1 }
				);
				expect(trim(result.filecontent)).tobe("Throws error for large number");
			});
			it( title="checking large number value in query with using the cfqueryparam", skip="#notHasMSSQL()#",  body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV4137.cfm",
					forms : { scene = 2 }
				);
				expect(trim(result.filecontent)).tobe("Throws error for large number with queryparam");
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