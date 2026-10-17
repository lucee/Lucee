component extends = "org.lucee.cfml.test.LuceeTestCase" labels="http"{

    variables.updateProvider = server.getTestService("updateProvider").url;

    function run( testResults, textbox ) {
        describe("testcase for LDEV-3847", function() {
            it(title="Checking attributeCollection inside CFFINALLY", body=function( currentSpec ) {
                var attrs = {result="local.res"};
                try {
                    var value = "";
                }
                finally {
                    cfhttp(url="#variables.updateProvider#/rest/update/provider/echoGet", attributeCollection="#attrs#") {
                    }  
                }
                expect(structKeyExists(local, "res")).toBeTrue();
            });

            it(title="attributeCollection of a query inside finally (script)", body=function( currentSpec ) {
                var qry = queryNew( "a", "varchar", [ [ "x" ], [ "y" ] ] );
                var attrs = { name: "local.res", dbtype: "query" };
                try {
                    var value = "";
                }
                finally {
                    query attributeCollection=attrs {
                        echo( "select * from qry" );
                    }
                }
                expect( res.recordcount ).toBe( 2 );
            });

            it(title="attributeCollection of savecontent inside finally after a catch (script)", body=function( currentSpec ) {
                var attrs = { variable: "local.res" };
                try {
                    throw "test";
                }
                catch ( any e ) {
                    var value = "";
                }
                finally {
                    savecontent attributeCollection=attrs {
                        echo( "inside finally" );
                    }
                }
                expect( res ).toBe( "inside finally" );
            });

            it(title="attributeCollection of a query inside cffinally (tag)", body=function( currentSpec ) {
                var result = _internalRequest(
                    template : "#createURI("LDEV3847")#/test.cfm"
                );
                expect( trim( result.filecontent ) ).toBe( "2" );
            });
        });
    }

    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}
