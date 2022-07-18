component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {
	function beforeAll(){
		variables.uri = createURI( "LDEV3207" );
	}
	function run( testResults, textbox ) {
		describe("testcase for LDEV-3207", function(){
			it(title = "query IN operator with queryparam list=true and value via valuelist", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV3207.cfm",
					forms : { Scene = 1 }
                );
				expect(trim(result.fileContent)).toBe("success");
			});

			it(title = "query IN operator with queryparam list=true and empty value via valuelist", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV3207.cfm",
					forms : { Scene = 2 }
				);
				expect(trim(result.fileContent)).toBe("success");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}