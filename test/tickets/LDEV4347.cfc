component extends = "org.lucee.cfml.test.LuceeTestCase" labels="loop" {

    function beforeAll() {
        application name="test-4347";
        application["simple"] = "test";
        application["array"] = [];
        application["struct"] = {};
    }

    function run( testResults, textbox ) {
        describe("Testcase for LDEV-4347", function() {
            it(title="Struct loop - delete the key from the same struct", body=function( currentSpec ) {
                try {
                    var res = "successfully deleted";
                    loop struct=application index="local.k" item="local.v" {
                        if (!isSimpleValue(v)) {
                            structDelete(application, k);
                        }
                    }
                }
                catch(any e) {
                    var res = e.message;
                }
                expect(res).toBe("successfully deleted");
            });
        });
    }
}