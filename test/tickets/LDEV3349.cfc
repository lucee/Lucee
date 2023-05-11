component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	function run( testResults, testBox ){

        describe( "checking query string encoding for spaces", function() {
            variables.uri = createURI("LDEV3349/test.cfm");

            it( title="cfhttp with queryString %20", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "%20"
                );
                expect(result.filecontent).toBe("%20");
            });
            it( title="cfhttp with queryString space", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: " "
                );
                expect(result.filecontent).toBe("%20");
            });
            it( title="cfhttp with queryString +", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "+"
                );
                expect(result.filecontent).toBe("%20");
            });
            it( title="cfhttp with queryString +%20", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "+%20"
                );
                expect(result.filecontent).toBe("%20%20");
            });
            it( title="cfhttp with queryString %2B+%2B", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: "%2B+%2B"
                );
                expect(result.filecontent).toBe("%2B%20%2B");
            });
            it( title="cfhttp with queryString space1space", body=function( currentSpec ){
                local.result = _internalRequest(
                    template: variables.uri,
                    url: " 1 "
                );
                expect(result.filecontent).toBe("%2B%20%2B");
            });
        });
    }

    private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & arguments.calledName;
	}
}