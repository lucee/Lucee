component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe(title="Testcase for LDEV-3338", body=function( currentSpec ) {
            variables.CFCwithoutSpace = getComponentMetadata(new LDEV3338.IMPLwithoutSpace());
            variables.CFCwithSpace = getComponentMetadata(new LDEV3338.IMPLwithSpace());
            it(title="Check getComponentMetadata() result with CFC implements attribute without spaces", body=function( currentSpec )  {
                expect(structKeyExists(CFCwithoutSpace.implements,"A")).toBe(true);
                expect(structKeyExists(CFCwithoutSpace.implements,"B")).toBe(true);
            });
            it(title="Check getComponentMetadata() result with CFC implements attribute with spaces", body=function( currentSpec )  {
                expect(structKeyExists(CFCwithSpace.implements,"A")).toBe(true);
                expect(structKeyExists(CFCwithSpace.implements,"B")).toBe(true);
            });
            it(title="Check getComponentMetadata() result with interface extends attribute without spaces", body=function( currentSpec )  {
                expect(structKeyExists(CFCwithoutSpace.implements.A.extends,"X")).toBe(true);
            });
            it(title="Check getComponentMetadata() result with interface extends attribute with spaces", body=function( currentSpec )  {
                expect(structKeyExists(CFCwithoutSpace.implements.B.extends,"X")).toBe(true);
            });
        });
    }
}