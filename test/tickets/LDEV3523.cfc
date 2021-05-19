component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe("Testcase for LDEV3523", function(){
            var comp = new LDEV3523.testComp();
            it( title="Check the component local scope variable with null value", body=function( currentSpec ){
                expect(comp.testFunc()).toBeNull();
            });
            it( title="Check the component local scope variable as null value with scope prefix and same variable in URL scope", body=function( currentSpec ){
                url.nullVar = "value from the URL scope"
                expect(comp.testScopeWithPrefix()).toBeNull();
            });
            it( title="Check the component local scope variable as null value with scope prefix and same variable in FORM scope", body=function( currentSpec ){
                Form.FormNullVar = "value from the FORM scope"
                expect(comp.testScopeWithPrefix()).toBeNull();
            });
            it( title="Check the component local scope variable as null value with same variable in URL scope", body=function( currentSpec ){
                url.nullVar = "value from the URL scope"
                expect(comp.testFunc()).toBeNull();
            });
            it( title="Check the component local scope variable as null value with same variable in FORM scope", body=function( currentSpec ){
                Form.FormNullVar = "value from the FORM scope"
                expect(comp.testFormScopeFunc()).toBeNull();
            });
        });
    }
}