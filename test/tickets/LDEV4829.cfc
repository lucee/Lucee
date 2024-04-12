component extends="org.lucee.cfml.test.LuceeTestCase" skip=true labels="qoq" {

    function beforeAll() {
        myQuery = queryNew("number,id,name,amount","Varchar,Integer,Varchar,Integer",
        [
            {number="3*562",id=1,name="one",amount=15},
            {number="96A562",id=2,name="Two",amount=18},
            {number="634*24",id=3,name="Three",amount=32}
        ]);

        myQuery2 = queryNew("number1,id1,name1,amount1","Varchar,Integer,Varchar,Integer",
        [
            {number1="634*24",id1="3",name1="Three",amount1=32}
        ]);
    };

    function run( testResults , testBox ) {
        describe( title="Test suite for LDEV-4829", body=function() {
            it( title = "Checking qoq with LIKE operator", body = function( currentSpec ) {

                sql = "
                    select id,number
                    from myQuery
                    where number LIKE '%3*562%'
                ";

                params = {};
                queryOptions = {dbtype="query"};
                myresult = queryExecute(sql,params,queryOptions);
                res = queryToStruct(myresult,"id");

                expect(myresult.recordCount).toBe(1);
                expect(res[1].number).toBe("3*562");
            });

            it( title = "Checking qoq with LIKE operator on hsqldb", body = function( currentSpec ) {

                sql = "
                    select myQuery.id,myQuery.number
                    from myQuery, myQuery2
                    where myQuery.number LIKE '%3*562%'
                "; // join to force hsqldb

                params = {};
                queryOptions = {dbtype="query"};
                myresult = queryExecute(sql,params,queryOptions);
                res = queryToStruct(myresult,"id");

                expect(myresult.recordCount).toBe(1);
                expect(res[1].number).toBe("3*562");

            });
        });
    }
}
