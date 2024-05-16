component extends="org.lucee.cfml.test.LuceeTestCase" labels="mssql,orm" {
    function beforeAll(){
        variables.uri = createURI("LDEV3860");
        cleanup();
    }

    function afterAll(){
        cleanup();
    }

    private function cleanUp(){
        if (!isDatasourceNotConfigured()){
            queryExecute( sql="DROP TABLE IF EXISTS testLDEV3680", options: {
                datasource: server.getDatasource("mssql") 
             }); 
        }
    }
    
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3859 & LDEV-3860", function() {
            it( title="LDEV-3860 -- Checking the exception occurred in transaction with ORM", skip="#isDatasourceNotConfigured()#", body=function( currentSpec ) {
                    local.result = _InternalRequest(
                        template : "#uri#\LDEV3860.cfm",
                        forms = {scene:1}
                    );
                expect(trim(result.filecontent)).toInclude("foo");
            });
            it( title="LDEV-3859 -- Checking the multiple transactions with ORM and query", skip="#isDatasourceNotConfigured()#", body=function( currentSpec ) {
                    local.result = _InternalRequest(
                        template : "#uri#\LDEV3859.cfm"
                    );
                expect(trim(result.filecontent)).toBe("true");
            });
        });
        // Testcase for LDEV3860 with some more cases ( disabled )
        describe(title="Testcase for LDEV-3860", body=function() {
            it( title="check error - which occurred before the ORM stuff inside transaction", skip="#isDatasourceNotConfigured()#", body=function( currentSpec ){

                local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:4}
                );
                expect( res.filecontent.trim() ).toInclude("foo");
            });
            it( title="check error - which occurred after the ORM stuff inside transaction", skip="#isDatasourceNotConfigured()#", body=function( currentSpec ){
                 local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:5}
                );
                expect( res.filecontent.trim() ).toInclude("foo");
            });
            it( title="check error - which occurred after the ORM stuff with datasource query inside transaction", skip="#isDatasourceNotConfigured()#", body=function( currentSpec ){
                 local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:6}
                );
                expect( res.filecontent.trim() ).toInclude("foo");
            });
            it( title="check error - which occurred after the ormGetSession() with datasource query inside transaction", skip="#isDatasourceNotConfigured()#", body=function( currentSpec ){
                 local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:7}
                );
                expect( res.filecontent.trim() ).toInclude("foo");
            });
        });

        // testcase for LDEV-4211
        describe(title="Testcase for LDEV-4211", skip=true, body=function() { 
             it( title="check error - using invalid entity name in entityNew() without transcation", skip="#isDatasourceNotConfigured()#", body=function( currentSpec ){
                local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:2}
                );
                expect(res.filecontent ).toInclude("no entity");
            });
            it( title="check error - using invalid entity name in entityNew() inside transcation", skip="#isDatasourceNotConfigured()#", body=function( currentSpec ){
                local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:3}
                );  
                expect( res.filecontent ).toInclude("no entity");
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
