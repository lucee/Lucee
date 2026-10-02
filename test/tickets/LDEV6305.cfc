component extends = "org.lucee.cfml.test.LuceeTestCase" labels="mssql" {

	function beforeAll(){
		variables.uri = createURI("LDEV6305");
	}

	function run( testResults, testBox ){
		describe("Test case for LDEV6305", function(){
			it( title = "CF_SQL_FLOAT Should Return Records for Matching Decimal Values", skip=isNotSupported(), body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/ldev6305.cfm"
				);
				expect(trim(result.filecontent)).toBe(1);
			});
		});
	}

	private function isNotSupported() {
		return isEmpty(server.getDatasource("mssql"));
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}