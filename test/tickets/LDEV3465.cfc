component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe("Testcase for LDEV-3465", function() {
            it( title="Access static variable directly", body=function( currentSpec ){
                res = new ldev3465.parent().getDirectStaticVariable( "parentStatic" );
                expect(res).toBe("static_variable_from_Parent");
            });
            it( title="Access static variable from child component", body=function(){
                try{
                    res = new ldev3465.child().getStaticVariable( "parentStatic" );
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe("static_variable_from_Parent");
            });
            it( title="Calling static method using dot notation", body=function(){
                try{
                    res = new ldev3465.parent().anotherStaticMethod();
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe("From_another_static_method");
            });
            it( title="Calling static method using dot notation which calls another static method", body=function(){
                try{
                    res = new ldev3465.parent().staticMethod();
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe("From_another_static_method");
            });
        });
    }
}