component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm,mysql" {
    function beforeAll() {
        variables.uri = createURI("LDEV3980");
        cleanup();
    }

    function afterAll() {
        cleanup();
    }

    private function cleanUp() {
        if (!notHasMysql()) {
            queryExecute( sql="DROP TABLE IF EXISTS LDEV3890", options: {
                datasource: server.getDatasource("mysql")
            }); 
        }
    }
    
    function run( testResults, testBox ) {
        describe("Testcase for LDEV3980", function() {
            it( title="ORM entityNew() within transaction", skip="#notHasMysql()#", body=function( currentSpec ) {
                local.result = _InternalRequest(
                    template : "#uri#\LDEV3980.cfm",
                    forms = {scene:1}
                );
                expect(result.filecontent).toBe("success");
            });
            it( title="ORM entityNew with properties within transaction", skip="#notHasMysql()#", body=function( currentSpec ) {
                local.result = _InternalRequest(
                    template : "#uri#\LDEV3980.cfm",
                    forms = {scene:2}
                );
                expect(result.filecontent).toBe("success");
            });
            it( title="ORM entityNew and entitySave within transaction", skip="#notHasMysql()#", body=function( currentSpec ) {
                local.result = _InternalRequest(
                    template : "#uri#\LDEV3980.cfm",
                    forms = {scene:3}
                );
                expect(result.filecontent).toBe("success");
            });
        });
    }
        
    private string function createURI(string calledName) {
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }

    private boolean function notHasMysql() {
        return !structCount(server.getDatasource("mysql"));
    }
}