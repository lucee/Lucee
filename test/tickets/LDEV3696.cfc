component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run( testResults, testBox ){
        describe(title="Testcase for LDEV-3696", body=function( currentSpec ) {
            it(title="Checking closure variable scope inside another closure/UDF", body=function( currentSpec, foo = "FOO from argument scope" )  {
                variables.foo = "FOO from variables scope";
                variables.bar = "BAR from variables scope";
                res1 = variables.foo;
                function (){
                    res2 = variables.bar;
                    res3 =  variables.foo;
                }();
                expect(res1).toBe("FOO from variables scope");
                expect(res2).toBe("BAR from variables scope");
                expect(res3).toBe("FOO from variables scope");
            });
        });
    }
}