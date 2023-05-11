component extends = "org.lucee.cfml.test.LuceeTestCase" skip = true{
    function run( testResults, textbox ) {
        describe("testcase for LDEV-3786", function(){
            it(title="Checking Comparison and arithmetic operations with char", body=function( currentSpec ){
                c = javacast("char", "1")
                expect(c + 10).toBe(59);
                expect(c + 0 > 10).toBe(true);
                expect(c > 10).toBe(true);
                expect(c == 49).toBe(true);
                expect(c < 49).toBe(false);
            });
        });
    }
}