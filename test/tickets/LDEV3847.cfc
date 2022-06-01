component extends = "org.lucee.cfml.test.LuceeTestCase" skip = true{
    function run( testResults, textbox ) {
        describe("testcase for LDEV-3847", function() {
            it(title="Checking attributeCollection inside CFFINALLY", body=function( currentSpec ) {
                var attrs = {result="local.res"};
                try {
                    var value = "";
                }
                finally {
                    cfhttp(url="https://update.lucee.org/rest/update/provider/echoGet", attributeCollection="#attrs#") {
                    }  
                }
                expect(structKeyExists(local, "res")).toBeTrue();
            });
        });
    }
}