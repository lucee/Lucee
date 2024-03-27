component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {

    function run( testResults, testBox ){
        describe( "Testcase for LDEV-4680", function(){
            it( "Testcase for LDEV-4680", function() {
                var users  = queryNew("id, firstname", "integer, varchar", [
                    {"id":1, "firstname":"Han"},
                    {"id":2, "firstname":"Han"},
                    {"id":3, "firstname":"James"}
                ]);
                var u1= queryExecute( "select firstname name from users group by name", {}, { dbtype="query" } );
                expect ( u1.recordcount ).toBe( 2 );
                var u2= queryExecute( "select firstname name from users group by firstname", {}, { dbtype="query" } );
                expect ( u2.recordcount ).toBe( 2 );
            });
        });
    }
}
