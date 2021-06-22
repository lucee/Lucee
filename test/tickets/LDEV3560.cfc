component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe(title="Testcase for LDEV-3560", body=function( currentSpec ) {
            it(title="getComponentMetadata() with abstract Component", body=function( currentSpec )  {
                    try {
                        metadata = getComponentMetadata("LDEV3560.testAbstract");
                        res = metadata.abstract;
                    }
                    catch(any e) {
                        res = e.message;
                    }
                    expect(res).toBe(true);
            });
        });
    }
}