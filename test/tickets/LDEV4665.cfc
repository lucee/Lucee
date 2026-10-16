component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

    function beforeAll(){
        variables.qry = queryNew(
            "id",
            "cf_sql_varchar",
            [
                ["01"],
                ["002"],
                ["0003"]
            ]
        );
    }

    function run( testResults , testBox ) {
        describe( title="Testcase for LDEV-4665", body = function() {

            it(title = "Checking queryFilter() function", body = function( currentSpec ) {
                var test = queryFilter(qry,(row) => true).columnData("id");
                expect( test[3] ).toBe( qry.id[3] );
                expect( test[3].len() ).toBe( qry.id[3].len() );
            });

            it(title = "Checking queryFilter() with member function", body = function( currentSpec ) {
                var test = qry.filter((row) => true).columnData("id");
                expect( test[3] ).toBe( qry.id[3] );
                expect( test[3].len() ).toBe( qry.id[3].len() )
            });

            it(title = "Checking queryMap() function", body = function( currentSpec ) {
                var test = queryMap(qry,(row) => row).columnData("id");
                expect( test[3] ).toBe( qry.id[3] );
                expect( test[3].len() ).toBe( qry.id[3].len() )
            });

            it(title = "Checking queryMap() with member function", body = function( currentSpec ) {
                var test = qry.map((row) => row).columnData("id");
                expect( test[3] ).toBe( qry.id[3] );
                expect( test[3].len() ).toBe( qry.id[3].len() )
            });

            it(title = "Checking querySlice() function", body = function( currentSpec ) {
                var test = querySlice(qry, 2).columnData("id");
                expect( test[2] ).toBe( qry.id[3] );
                expect( test[2].len() ).toBe( qry.id[3].len() );
            });

            it(title = "Checking queryReverse() function", body = function( currentSpec ) {
                var test = queryReverse(qry).columnData("id");
                expect( test[1] ).toBe( qry.id[3] );
                expect( test[1].len() ).toBe( qry.id[3].len() );
            });

            it(title = "Checking the column type is kept", body = function( currentSpec ) {
                var type = getMetadata(qry)[1].typeName;
                expect( getMetadata(qry.filter((row) => true))[1].typeName ).toBe( type );
                expect( getMetadata(qry.map((row) => row))[1].typeName ).toBe( type );
                expect( getMetadata(querySlice(qry, 1))[1].typeName ).toBe( type );
                expect( getMetadata(queryReverse(qry))[1].typeName ).toBe( type );
            });
        });
    }
}
