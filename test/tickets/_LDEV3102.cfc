component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.uri = createURI( "LDEV3102" );
	}
	function run( testResults, textbox ) {
		describe("testcase for LDEV-3102", function(){
			it(title = "Select operation in cfquery with name and result attribute", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 1 }
				);
				expect(trim(result.fileContent)).toBe(1);
			});

			it(title = "Insert and Select operation in cfquery with name and result attribute", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 1 , insert = true }
				);
				expect(trim(result.fileContent)).toBe(2);
			});

			it(title = "Select and Insert operation in cfquery with name attribute", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 2 }
				);
				expect(trim(result.fileContent)).toBe(2);
			});

			it(title = "Select and Insert operation in cfquery with name and result attribute", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 3}
				);
				expect(trim(result.fileContent)).toBe(1);
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}