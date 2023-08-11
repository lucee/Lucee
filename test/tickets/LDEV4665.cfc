component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

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
        });
    }
}
