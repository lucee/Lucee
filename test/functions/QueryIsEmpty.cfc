component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults, testBox ){
        describe(title="Testcase for QueryIsEmpty", body=function( currentSpec ) {
            variables.qryNoColumm = queryNew("");
            variables.qryNoRecord = queryNew("name", "varchar", []);
            variables.qry = queryNew("name", "varchar", ["test"]);
            it(title="Checking function QueryIsEmpty()", body=function( currentSpec )  {
                assertEquals(true, QueryIsEmpty(qryNoColumm));
                assertEquals(true, QueryIsEmpty(qryNoRecord));
                assertEquals(false, QueryIsEmpty(qry));
            });
            it(title="Checking member function query.isEmpty()", body=function( currentSpec )  {
                assertEquals(true, qryNoColumm.isEmpty());
                assertEquals(true, qryNoRecord.isEmpty());
                assertEquals(false, qry.isEmpty());
            });
        });
    }
}