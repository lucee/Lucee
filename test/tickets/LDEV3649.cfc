component extends = "org.lucee.cfml.test.LuceeTestCase" labels="orm" {

    function beforeAll() {
        variables.uri = createURI("LDEV3649");
    }

    function run( testResults , testBox ) {
        describe( "Test case for LDEV-3900", function() {
            it( title="Check importDefinitions of entity component with entityNew()", skip=noOrm(), body=function( currentSpec ) {
                local.result = _internalRequest(
                    template="#uri#/LDEV3649.cfm",
                    forms={ scene:1 }
                )
                expect(trim(result.fileContent)).toBe("import component worked");
            });
            it( title="Check importDefinitions of entity component with entityLoad()", skip=noOrm(), body=function( currentSpec ) {
                local.result = _internalRequest(
                    template="#uri#/LDEV3649.cfm",
                    forms={ scene:2 }
                )
                expect(trim(result.fileContent)).toBe("import component worked");
            });
        });
    }

    private string function createURI(string calledName) {
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }

    private function noOrm() {
        return ( structCount( server.getTestService("orm") ) eq 0 );
    }
}