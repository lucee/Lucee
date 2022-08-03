component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {
    function beforeAll() {
        variables.uri = createURI("LDEV3070");
    }

    function afterAll() {
        cleanup();
    }

    private function cleanUp() {
        if (!notHasMysql()) {
            queryExecute( sql="DROP TABLE IF EXISTS LDEV3070", options: {
                datasource: server.getDatasource("mysql")
            }); 
        }
    }
    
    function run( testResults, testBox ) {
        describe("Testcase for LDEV3070", function() {
            it( title="checking datasource name of cfquery with return type query", skip="#notHasMysql()#", body=function( currentSpec ) {
                expect(makeInternalRequest("cfquery", "query")).toBe("LDEV3070");
            });
            it( title="checking datasource name of cfquery tag with return type array", skip="#notHasMysql()#", body=function( currentSpec ) {
                expect(makeInternalRequest("cfquery", "array")).toBe("LDEV3070");   
            });
            it( title="checking datasource name of cfquery tag with return type struct", skip="#notHasMysql()#", body=function( currentSpec ) {
                expect(makeInternalRequest("cfquery", "struct")).toBe("LDEV3070"); 
            });

            it( title="checking datasource name of queryExecute() with return type query", skip="#notHasMysql()#", body=function( currentSpec ) {
                expect(makeInternalRequest("queryExecute", "query")).toBe("LDEV3070");
            });
            it( title="checking datasource name of queryExecute() with return type array", skip="#notHasMysql()#", body=function( currentSpec ) {
                expect(makeInternalRequest("queryExecute", "array")).toBe("LDEV3070");   
            });
            it( title="checking datasource name of queryExecute() with return type struct", skip="#notHasMysql()#", body=function( currentSpec ) {
                expect(makeInternalRequest("queryExecute", "struct")).toBe("LDEV3070"); 
            });
            
            it( title="checking datasource name of cfquery with dbtype=query", skip="#notHasMysql()#", body=function( currentSpec ) {
                expect(makeInternalRequest("QoQ", "query")).toBe("true"); 
            });
        });
    }


    private string function makeInternalRequest(required string scene, required string returnType) {
        var result = _InternalRequest(
            template : "#uri#/LDEV3070.cfm",
            forms : {scene: arguments.scene, returnType: arguments.returnType}
        );
        return result.filecontent.trim();
    }
        
    private boolean function notHasMysql() {
        return !structCount(server.getDatasource("mysql"));
    }

    private string function createURI(string calledName) {
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }

}
