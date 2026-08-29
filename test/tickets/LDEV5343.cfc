component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
    function beforeAll(){
		variables.uri = createURI("LDEV5343");
	}

    function run(testResults, testBox) {
        describe( title = "Test case for LDEV-5343 Null coalescing operator (??)", body = function() {
            var defaultValue = "lucee";
            var initialValue = "CFML";
            it( title = "should return the default value when the left side is null", body = function( currentSpec ) {
                local.result = _InternalRequest(
					template:"#variables.uri#\ldev5343.cfm",
                    forms:{defaultValue = defaultValue}
				);
                expect( local.result.filecontent.trim() ).toBe( 'lucee' );
            });

            it( title = "should return the default value when the variable is undefined", body = function( currentSpec ) {
                local.result = _InternalRequest(
					template:"#variables.uri#\ldev5343.cfm",
                    forms:{defaultValue = defaultValue, undefined = true}
				);
                expect( local.result.filecontent.trim() ).toBe( 'lucee' );
            });

            it( title = "should return the left value when it is not null", body = function( currentSpec ) {
                  local.result = _InternalRequest(
					template:"#variables.uri#\ldev5343.cfm",
                    forms:{defaultValue = defaultValue, initialValue = "CFML"}
				);
                expect( local.result.filecontent.trim() ).toBe( 'CFML' );
            });

        });
    }
    private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}