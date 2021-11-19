component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll() {
		variables.uri = createURI("LDEV3309");
	}
	function run( testResults, testBox ){
        describe("Testcase for LDEV3309", function(){
            it(title="Assign value to url scope using cfset", body=function( currentSpec ){
                local.result = _InternalRequest(
					template : "#uri#\test.cfm",
                    FORM : { scene : 1 }
				);
                expect(trim(result.fileContent)).toBe("url.set from custom tag");
            });
            it(title="Assign value to url scope using cfparam", body=function( currentSpec ){
                local.result = _InternalRequest(
					template : "#uri#\test.cfm",
                    FORM : { scene : 2 }
				);
                expect(trim(result.fileContent)).toBe("url.param from custom tag");
            });
            it(title="Assign value to url scope using setVariable()", body=function( currentSpec ){
                local.result = _InternalRequest(
					template : "#uri#\test.cfm",
                    FORM : { scene : 3 }
				);
                expect(trim(result.fileContent)).toBe("url.setVariable from custom tag");
            });
        });
    }
    private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}