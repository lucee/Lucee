component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe(title="Testcase for LDEV-3670", body=function( currentSpec ) {
            it(title="getComponentMetadata() with final Component", body=function( currentSpec )  {
                try {
                    obj = new LDEV_3670.testFinal();
                    metadata = getComponentMetadata(obj);
                    res = metadata.final;
                }
                catch(any e) {
                    res = e.message;
                }
                expect(res).toBe(true);
            });
        });
    }
}