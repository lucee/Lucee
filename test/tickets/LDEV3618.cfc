component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{

    function beforeAll(){
        variables.uri = createURI("LDEV3618");
    }

    function run( testResults , testBox ) {
        describe( "This test case suit for LDEV-3618 ", function(){

            it( title = "cfhtmlhead only with body-Content", body = function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : {scene = 1}
                );
                expect(trim(result.filecontent)).toBe("Body-content without text attribute");
            });

            it( title = "cfhtmlhead only with text attribute", body = function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : {scene = 2}
                );
                expect(trim(result.filecontent)).toBe("Text without body-content");
            });

            it( title = "cfhtmlhead with both text attribute and body-content", body = function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : {scene = 3}
                );
                expect(trim(result.filecontent)).toBe("Text-content, Body-content");
            });
        });
    }

    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
} 