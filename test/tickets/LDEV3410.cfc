component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe(title="Testcase for LDEV-3410", body=function( currentSpec ) {
            variables.Test = new LDEV3410.Test();
            it(title="Check SerializeJSON() result with correct booleans", body=function( currentSpec )  {
                local.result = serializeJSON(Test);
                expect(local.result).toBe("{""booleanValue1"":true,""booleanValue2"":false}");
            });
        });
    }
}
