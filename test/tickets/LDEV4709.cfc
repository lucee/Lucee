component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {

    function run( testResults, testBox ){
        describe( "Testcase for LDEV-4709", function(){
            it( title="Checking lambda expressions with isclosure() function", body=function( currentSpec ) {
                udf = () => { return "foo"; };
                expect (isClosure(udf)).toBe(true);
                udf = function(){ return "foo"; };
                expect (isClosure(udf)).toBe(true);
            });
        });
    }
}
