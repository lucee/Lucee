component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

    function beforeAll() {
        variables.myQuery = queryNew("number,id,name,amount","Varchar,Integer,Varchar,Integer",
        [
            {number="3*562",id=1,name="one",amount=15},
            {number="96A562",id=2,name="Two",amount=18},
            {number="634*24",id=3,name="Three",amount=32}
        ]);

        variables.myQuery2 = queryNew("number1,id1,name1,amount1","Varchar,Integer,Varchar,Integer",
        [
            {number1="634*24",id1="3",name1="Three",amount1=32}
        ]);

        variables.operator = "|";

        variables.myQuery3 = queryNew("number,id,name,amount","Varchar,Integer,Varchar,Integer",
        [
            {number="3#variables.operator#562",id=1,name="one",amount=15},
            {number="562",id=1,name="one",amount=15},
            {number="3",id=1,name="one",amount=15},
            {number="96A562",id=2,name="Two",amount=18},
            {number="634*24",id=3,name="Three",amount=32}
        ]);
        
        variables.myQuery4 = queryNew("number1,id1,name1,amount1","Varchar,Integer,Varchar,Integer",
        [
            {number1="634#variables.operator#24",id1="3",name1="Three",amount1=32}
        ]);
    };

    function run( testResults , testBox ) {
        describe( title="Test suite for LDEV-4829, qoq LIKE operator with *", body=function() {
            it( title = "Checking qoq with LIKE operator with *", body = function( currentSpec ) {
                var sql = "
                    select  id,number
                    from    myQuery
                    where   number LIKE '%3*562%'
                ";
                var params = {};
                var queryOptions = { dbtype="query" };
                var myresult = queryExecute( sql, params, queryOptions );
                var res = queryToStruct( myresult, "id" );
                expect( myresult.recordCount ).toBe( 1 );
                expect( res[ 1 ].number ).toBe( "3*562" );
            });

            it( title = "Checking qoq with LIKE operator on hsqldb with *", body = function( currentSpec ) {

                var sql = "
                    select  myQuery.id,myQuery.number
                    from    myQuery, myQuery2
                    where   myQuery.number LIKE '%3*562%'
                "; // join to force hsqldb
                var params = {};
                var queryOptions = { dbtype="query" };
                var myresult = queryExecute( sql, params, queryOptions );
                var res = queryToStruct( myresult, "id" );
                expect( myresult.recordCount).toBe( 1 );
                expect( res[ 1 ].number ).toBe( "3*562" );
            });
        });

        describe( title="Test suite for LDEV-4829, qoq LIKE operator with |", body=function() {
            it( title = "Checking qoq with LIKE operator with |", body = function( currentSpec ){
                var sql = "
                    select  id, number
                    from    myQuery3
                    where   number LIKE '%3#variables.operator#562%'
                ";
                var params = {};
                var queryOptions = { dbtype="query" };
                var myresult = queryExecute( sql, params, queryOptions );
                var res = queryToStruct( myresult, "id" );

                expect( myresult.recordCount ).toBe( 1 );
                expect( res[ 1 ].number ).toBe( "3#variables.operator#562" );
            });

            it( title = "Checking qoq with LIKE operator on hsqldb with |", body = function( currentSpec ) {
                var sql = "
                    select  myQuery3.id,myQuery3.number
                    from    myQuery3, myQuery4
                    where   myQuery3.number LIKE '%3|562%'
                "; // join to force hsqldb
                var params = {};
                var queryOptions = { dbtype="query" };
                var myresult = queryExecute( sql, params, queryOptions );
                var res = queryToStruct( myresult, "id" );

                expect( myresult.recordCount ).toBe( 1 );
                expect( res[1].number ).toBe( "3#variables.operator#562" );

            });
        });
    }
}
