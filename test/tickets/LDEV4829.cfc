component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

    function run( testResults , testBox ) {
        describe( title="Test suite for LDEV-4829", body=function() {
            it( title = "Checking DbType query with LIKE operator", body = function( currentSpec ) {

                myQuery = queryNew("number,id,name,amount","Varchar,Integer,Varchar,Integer",
                [
                    {number="3*562",id=1,name="one",amount=15},
                    {number="96A562",id=2,name="Two",amount=18},
                    {number="634*24",id=3,name="Three",amount=32}
                ]);

                sql = "
                    select id,number
                    from myQuery
                    where number LIKE '%3*562%'
                ";

                params = {};
                queryOptions = {dbtype="query"};
                myresult = queryExecute(sql, params,queryOptions);
                res = queryToStruct(myresult,"id");

                expect(myresult.recordCount).toBe(1);
                expect(res[1].number).toBe("3*562");
            });
        });
    }
}
