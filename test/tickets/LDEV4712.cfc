component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {

    function run( testResults, testBox ){
        describe( "Testcase for LDEV-4712", function(){
            it( title="checking if the udf cache reconize default values", body=function( currentSpec ) {
                var res1=testCache(); // use default value
                var res2=testCache(1); // regular argument
                var res3=testCache(arg=1); // named argument
                
                expect (res1).toBe(res2);
                expect (res2).toBe(res3);
            });
        });
    }


    private function testCache(arg=1) cachedwithin=createTimeSpan(0,0,0,10) {
        return "cid:"&createUniqueID();
    }
}
