component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe("Testcase for LDEV-3362", function( currentSpec ) {
            it(title="Checking static and non-static functions in getComponentMetaData()", body=function( currentSpec )  {
                metaData = getComponentMetaData("LDEV3362.test");
                expect(len(metaData.functions)).toBe(2);
                expect(metaData.functions[1].name).toBe("foo");
                expect(metaData.functions[2].name).toBe("fooStatic");
            });
        });
    }
}