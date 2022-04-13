component extends="org.lucee.cfml.test.LuceeTestCase" labels="mssql,orm" {
    function beforeAll(){
        variables.uri = createURI("LDEV3659");
        cleanup();
    }

    function afterAll(){
        cleanup();
    }

    private function cleanUp(){
        if (!isDatasourceNotConfigured()){
            queryExecute( sql="DROP TABLE IF EXISTS Persons", options: {
                datasource: server.getDatasource("mssql") 
             }); 
        }
    }
    
    function run( testResults, testBox ) {
        describe("Second Testcase for LDEV-3659", function() {
            it( title="LDEV-3659 -- Checking the mixed transactions with ORM and cfquery", skip="#isDatasourceNotConfigured()#", body=function( currentSpec ) {
                local.result = _InternalRequest(
                    template : "#uri#/index.cfm"
                );
                expect( trim( result.filecontent ) ).toBe( "Michael Born" );
            });
        });
    }

    private boolean function isDatasourceNotConfigured(){
        return !structCount(server.getDatasource("mssql"));
    } 

    private string function createURI(string calledName) {
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}