component extends="org.lucee.cfml.test.LuceeTestCase" skip=false {
    function run( testResults , testBox ) {
        describe( "test case for LDEV-3584", function() {
            it( title = "getComponentMetadata() of final CFC using CFC object", body=function( currentSpec ) {
                obj = new LDEV3584.test();
                objCFCMetaData = getComponentMetadata(obj);
                expect(objCFCMetaData.type).toBe("component");
            });
            it( title = "getComponentMetadata() of final CFC using CFC path", body=function( currentSpec ) {
                try{
                    pathCFCMetaData = getComponentMetadata("LDEV3584.test");
                }
                catch(any e){
                   pathCFCMetaData.type = e.message; 
                }
                expect(pathCFCMetaData.type).toBe("component");
            });
        });
    }
}