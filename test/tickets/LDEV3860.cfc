component extends="org.lucee.cfml.test.LuceeTestCase" {
    function beforeAll(){
        variables.uri = createURI("LDEV3860");
    }
    
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3859 & LDEV-3860", function() {
            it( title="LDEV-3860 -- Checking the exception occured in transaction with ORM", skip="#isDatasourceAvailable()#", body=function( currentSpec ) {
                    local.result = _InternalRequest(
                        template : "#uri#\LDEV3860.cfm",
                        forms = {scene:1}
                    );
                expect(trim(result.filecontent)).toBe("true");
            });
            it( title="LDEV-3859 -- Checking the multiple transactions with ORM and query", skip="#isDatasourceAvailable()#", body=function( currentSpec ) {
                    local.result = _InternalRequest(
                        template : "#uri#\LDEV3859.cfm"
                    );
                expect(trim(result.filecontent)).toBe("true");
            });
        });
        // Testcase for LDEV3860 with some more cases ( disabled )
        describe(title="Testcase for LDEV-3860", body=function() {
            it( title="check error - using invalid entity name in entityNew() without transcation", skip="#isDatasourceAvailable()#", body=function( currentSpec ){
                local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:2}
                );
                expect(res.filecontent ).toInclude("no entity");
            });
            it( title="check error - using invalid entity name in entityNew() inside transcation", skip="#isDatasourceAvailable()#", body=function( currentSpec ){
                local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:3}
                );
                expect( res.filecontent ).notToBe("true");
            });
            it( title="check error - which occured before the ORM stuff inside transcation", skip="#isDatasourceAvailable()#", body=function( currentSpec ){
                local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:4}
                );
                expect( res.filecontent.trim() ).notToBe("true");
            });
            it( title="check error - which occured after the ORM stuff inside transcation", skip="#isDatasourceAvailable()#", body=function( currentSpec ){
                 local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:5}
                );
                expect( res.filecontent.trim() ).notToBe("true");
            });
            it( title="check error - which occured after the ORM stuff with datasource query inside transcation", skip="#isDatasourceAvailable()#", body=function( currentSpec ){
                 local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:6}
                );
                expect( res.filecontent.trim() ).notToBe("true");
            });
            it( title="check error - which occured after the ormGetSession() with datasource query inside transaction", skip="#isDatasourceAvailable()#", body=function( currentSpec ){
                 local.res = _InternalRequest(
                    template="#variables.uri#\LDEV3860.cfm",
                    forms = {scene:7}
                );
                expect( res.filecontent.trim() ).notToBe("true");
            });
        });
    }

    private boolean function isDatasourceAvailable(){
        return !structCount(server.getDatasource("mssql"));
    } 

    private string function createURI(string calledName) {
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}