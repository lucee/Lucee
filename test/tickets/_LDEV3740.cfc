component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run( testResults, textbox ) {
        describe("testcase for LDEV-3740", function(){
            it(title="Checking UDF in tag island within callback", body=function( currentSpec ){
                callback = () => {
                    ```
                    <cffunction name="testFunctionInsideTagIsland">
                        <cfreturn "From tag island">
                    </cffunction>
                    ```
                    return "From callback";
                }
                expect(callback()).toBe("From callback");
                expect(isNull(testFunctionInsideTagIsland())).toBe(false);
                expect(testFunctionInsideTagIsland()).toBe("From tag island");
            });
        });
    }
}