component extends = "org.lucee.cfml.test.LuceeTestCase" {
    function beforeAll(){
        variables.uri = createURI("LDEV5620");
    }
    function run( testResults , testBox ) {
        describe( "Testcase for LDEV-5620", function() {

            it( title = "should load the internal Java class without error",  body = function( CurrentSpec ) {
                var result = "";
                try {
                    result = createObject("java", "org.mindrot.jbcrypt.BCrypt", expandPath("#variables.uri#/test.jar"));
                } catch (any e) {
                    result = e.stackTrace;
                }
                expect( isObject(result) ).toBeTrue();
                var className = result.getClass().getName();
                expect( className ).toBe( "org.mindrot.jbcrypt.BCrypt" );
            });

        });
    }
    private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
