component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{

    function beforeAll() {
        variables.uri = createURI("LDEV2250");
    }

    function run( testResults , testBox ) {
        describe( "Test suite for LDEV-2250", function() {
            it( title='Checking CFHTTPPARAM tag with encoded=false attribute', body=function( currentSpec ) {
                cfhttp(method="POST", url="http://#CGI.SERVER_NAME#/test/testcases/LDEV2250/LDEV2250.cfm") {
                    cfhttpparam( name="lucee", type="formfield", value="https://docs.lucee.org", encoded="false" );
                }
                expect(cfhttp.filecontent).toBe("lucee=https://docs.lucee.org");
            });

            it( title='Checking CFHTTPPARAM tag with encoded=true attribute', body=function( currentSpec ) {
                cfhttp(method="POST", url="http://#CGI.SERVER_NAME#/test/testcases/LDEV2250/LDEV2250.cfm") {
                    cfhttpparam( name="lucee", type="formfield", value="https://docs.lucee.org", encoded="true" );
                }
               expect(cfhttp.filecontent).toInclude("lucee=https%3A%2F%2Fdocs");
           });
        });
    }

    private string function createURI(string calledName) {
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}
