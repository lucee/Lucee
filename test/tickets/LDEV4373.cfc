component extends = "org.lucee.cfml.test.LuceeTestCase" labels="string" skip="true" {
    function run( testResults, textbox ) {
        describe("Testcase for LDEV-4373", function() {
            it(title="Extract the string using the array notation", body=function( currentSpec ) {
                var mystring = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                expect(mystring[4]).toBe("D");
            });
            it(title="Extract the string using the array notation with negative index", body=function( currentSpec ) {
                try {
                    var mystring = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                    var res = mystring[-3]
                }
                catch(any e) {
                    var res = e.message;
                }
                expect(res).toBe("X");
            });
        });
    }
}
