component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe(title="Testcase for isSimpleValue", body=function( currentSpec ) {
            it(title="Checking isSimpleaValue()", body=function( currentSpec )  {
                assertEquals(isSimpleValue("string"),true);
                assertEquals(isSimpleValue(123456),true);
                assertEquals(isSimpleValue(getTimezone()),true);
                assertEquals(isSimpleValue(now()),true);
                assertEquals(isSimpleValue(javacast("char","A")),true);
                assertEquals(isSimpleValue(nullValue()),false);
                assertEquals(isSimpleValue([]),false);
                assertEquals(isSimpleValue({}),false);
                assertEquals(isSimpleValue(queryNew("test")),false);
            });
        });
    }
}