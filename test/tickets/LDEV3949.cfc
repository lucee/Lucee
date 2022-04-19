component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults , testBox ) {
        describe( "Testcase for LDEV-3949", function() {
            it( title="listAppend() with empty string as list", body=function( currentSpec ){
                expect(listAppend("",",,,foo,,,bar",",",false)).toBe("foo,bar");
                expect(listAppend("",",,,foo,,,bar",",",true)).toBe(",,,foo,,,bar");
            });
            it( title="listPrepend() with empty string as list", body=function( currentSpec ){
                expect(listPrepend("",",,,foo,,,bar",",",true)).toBe(",,,foo,,,bar");
                expect(listPrepend("",",,,foo,,,bar",",",false)).toBe("foo,bar");
            });
        });
        describe( "Testcase for LDEV-3956", function() {
            it( title="listAppend() with empty string as delimiter", body=function( currentSpec ){
                expect(listAppend("",",,,foo,,,bar","",false)).toBe("");
                expect(listAppend("test,,",",,,foo,,,bar","",false)).toBe("test,,");
                expect(listAppend("",",,,foo,,,bar","",true)).toBe("");
                expect(listAppend("test,,",",,,foo,,,bar","",true)).toBe("test,,");
            });
            it( title="listPrepend() with empty string as delimiter", body=function( currentSpec ){
                expect(listPrepend("",",,,foo,,,bar","",false)).toBe(",,,foo,,,bar");
                expect(listPrepend("test",",,,foo,,,bar","",false)).toBe(",,,foo,,,bar");
                expect(listPrepend("",",,,foo,,,bar","",true)).toBe(",,,foo,,,bar");
                expect(listPrepend("test",",,,foo,,,bar","",true)).toBe(",,,foo,,,bar");
            });
        });
    }
}