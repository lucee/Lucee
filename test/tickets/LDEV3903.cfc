component extends="org.lucee.cfml.test.LuceeTestCase" skip="true"{
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3903", function() {
            it( title="Accessing the static variable declared inside static constructor", body=function( currentSpec ) {
                expect(LDEV3903.testComp::inConstructor).toBe("from staic constructor variable")
            });
            it( title="Accessing the static variable declared by using dot notation", body=function( currentSpec ) {
                try {
                    var result = LDEV3903.testComp::dotNotation;
                }
                catch(any e) {
                    var result = e.message;
                }
                expect(result).toBe("from dot notation static variable")
            });
        });
    }
}