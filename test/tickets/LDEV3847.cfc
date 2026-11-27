component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true labels="http"{

    variables.httpbin = server.getTestService("httpbin");
    variables.hasHttpbin = structCount(variables.httpbin) > 0;
    variables.httpbinUrl = variables.hasHttpbin ? "http://#variables.httpbin.server#:#variables.httpbin.port#" : "";

    function run( testResults, textbox ) {
        describe("testcase for LDEV-3847", function() {
            it(title="Checking attributeCollection inside CFFINALLY", body=function( currentSpec ) {
                if ( !variables.hasHttpbin ) return;
                var attrs = {result="local.res"};
                try {
                    var value = "";
                }
                finally {
                    cfhttp(url="#variables.httpbinUrl#/anything", attributeCollection="#attrs#") {
                    }  
                }
                expect(structKeyExists(local, "res")).toBeTrue();
            });
        });
    }
}