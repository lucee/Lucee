component extends="org.lucee.cfml.test.LuceeTestCase" labels="static,metadata"{
    function run( testResults, testBox ){
        describe("Testcase for LDEV-3362", function( currentSpec ) {
            it(title="Checking static and non-static functions in getComponentMetaData()", body=function( currentSpec )  {
                metaData = getComponentMetaData("LDEV3362.test");
                expect(metaData.functions[1].name).toBe("foo1_public");
                expect(metaData.functions[2].name).toBe("foo2_private");
                expect(metaData.functions[3].name).toBe("foo3_package");
                expect(metaData.functions[4].name).toBe("foo4_remote");
                expect(metaData.functions[5].name).toBe("foo5_staticPublic");
                expect(metaData.functions[6].name).toBe("foo6_StaticPrivate");
                expect(metaData.functions[7].name).toBe("foo7_StaticPackage");
                expect(metaData.functions[8].name).toBe("foo8_StaticRemote");
                expect(len(metaData.functions)).toBe(8);
            });
        });
    }
}