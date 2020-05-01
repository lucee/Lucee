component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV2566");
	}

	function run( testResults, testbox ){
		describe("Test case for LDEV2566", function(){
			it(title = "CFINSERT tag check with MSSQL", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : uri&"/mssql/test.cfm"
				);
				expect(trim(result.filecontent)).tobe("lucee");
			});

			it(title = "CFINSERT tag check with MYSQL", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : uri&"/mysql/test.cfm"
				);
				expect(trim(result.filecontent)).tobe("lucee_core");
			});

			it(title = "CFINSERT tag check with POSTGRESQL", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : uri&"/postgresql/test.cfm"
				);
				expect(trim(result.filecontent)).tobe("lucee_core_dev");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}