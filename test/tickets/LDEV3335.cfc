component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll() {
		variables.uri = createURI("LDEV3335");
	}
	function run( testResults, testBox ){
        describe( "Testcase for LDEV3335", function(){
            it( title="Check size of the component with no accessors", body=function( currentSpec ){
                local.result = _InternalRequest(
					template : "#uri#\test.cfm",
                    FORM : { scene : 1 }
				);
                expect(trim(result.fileContent)).toBeLT(1000);
            });
            it( title="Check size of the component with mannual setters/getters", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    FORM : { scene : 2 }
                );
                expect(trim(result.fileContent)).toBeLT(5000);
            });
            it( title="Check size of the component with accessors", body=function( currentSpec ){
                local.result = _InternalRequest(
					template : "#uri#\test.cfm",
                    FORM : { scene : 3 }
				);
                expect(trim(result.fileContent)).toBeLT(5000);
            });
        });
    }
    private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}