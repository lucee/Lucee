component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe("Testcase for LDEV-3604", function( currentSpec ) {
            it(title="Checking instance of original CFC", body=function( currentSpec )  {
                child = new LDEV3604.child();
                child.setFoo("bar");
                expect(serializeJSON(child.getInstance())).toBe('{"FOO":"bar"}');
            });
            it(title="Checking instance of clone CFC without mixins", body=function( currentSpec )  {
                cloneWithoutMixin = child.cloneWithoutMixin();
                expect(serializeJSON(cloneWithoutMixin.getInstance())).toBe('{"FOO":"bar"}');
            });
            it(title="Checking instance of clone CFC with mixins", body=function( currentSpec )  {
                clone = child.clone();
                expect(serializeJSON(clone.getInstance())).toBe('{"FOO":"bar"}');
            });
        });
    }
}