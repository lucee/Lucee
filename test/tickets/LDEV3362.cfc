component extends="org.lucee.cfml.test.LuceeTestCase" labels="static,metadata"{
    function run( testResults, testBox ){
        describe("Testcase for LDEV-3362", function( currentSpec ) {
            it(title="Checking static and non-static functions in getComponentMetaData()", body=function( currentSpec )  {
                metaData = getComponentMetaData("LDEV3362.test");
                var names=[];
                loop array=metaData.functions item="local.f" {
                    arrayAppend(names, f.name);
                }
                arraySort(names, "textnocase");
                expect(arrayToList(names))
                .toBe("foo1_public,foo2_private,foo3_package,foo4_remote,foo5_staticPublic,foo6_StaticPrivate,foo7_StaticPackage,foo8_StaticRemote");
            });
        });
    }
}