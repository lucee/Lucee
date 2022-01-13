component extends = "org.lucee.cfml.test.LuceeTestCase" {

    function beforeAll() {
        application enableNullSupport=true;
    }

    function afterAll() {
        application enableNullSupport=false;
    }

    function run( testResults, textbox ) {

        describe("testcase for LDEV-3734", function(){

            it(title="Arithmetic addition with NULL in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT 5+5 AS result,
                      NULL+5 as result2,
                      5+NULL as result3,
                      NULL+NULL as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 10 );
				expect( actual.result2 ).toBeNull();
				expect( actual.result3 ).toBeNull();
				expect( actual.result4 ).toBeNull();
            });

            it(title="Arithmetic subtraction with NULL in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT 20-10 AS result,
                      NULL-5 as result2,
                      5-NULL as result3,
                      NULL-NULL as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 10 );
				expect( actual.result2 ).toBeNull();
				expect( actual.result3 ).toBeNull();
				expect( actual.result4 ).toBeNull();
            });

            it(title="Arithmetic multiplication with NULL in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT 2*5 AS result,
                      NULL*5 as result2,
                      5*NULL as result3,
                      NULL*NULL as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 10 );
				expect( actual.result2 ).toBeNull();
				expect( actual.result3 ).toBeNull();
				expect( actual.result4 ).toBeNull();
            });

            it(title="Arithmetic division with NULL in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT 20/2 AS result,
                      NULL/5 as result2,
                      5/NULL as result3,
                      NULL/NULL as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 10 );
				expect( actual.result2 ).toBeNull();
				expect( actual.result3 ).toBeNull();
				expect( actual.result4 ).toBeNull();
            });

            it(title="Arithmetic bitwise with NULL in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT 4^2 AS result,
                      NULL^5 as result2,
                      5^NULL as result3,
                      NULL^NULL as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 6 );
				expect( actual.result2 ).toBeNull();
				expect( actual.result3 ).toBeNull();
				expect( actual.result4 ).toBeNull();
            });

            it(title="Arithmetic modulus with NULL in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                // Note % and mod() have different implemntations
                var actual = queryExecute(
                    "SELECT 21%11 AS result,
                      NULL%5 as result2,
                      5%NULL as result3,
                      NULL%NULL as result4,
                      mod( 21, 11 ) AS result5,
                      mod( NULL, 5 ) as result6,
                      mod( 5, NULL ) as result7,
                      mod( NULL, NULL ) as result8
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 10 );
				expect( actual.result2 ).toBeNull();
				expect( actual.result3 ).toBeNull();
				expect( actual.result4 ).toBeNull();
				expect( actual.result5 ).toBe( 10 );
				expect( actual.result6 ).toBeNull();
				expect( actual.result7 ).toBeNull();
				expect( actual.result8 ).toBeNull();
            });

            it(title="Arithmetic exponent with NULL in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT power( 4, 2 ) AS result,
                      power( NULL, 5 ) as result2,
                      power( 5, NULL ) as result3,
                      power( NULL, NULL ) as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 16 );
				expect( actual.result2 ).toBeNull();
				expect( actual.result3 ).toBeNull();
				expect( actual.result4 ).toBeNull();
            });

        });

    }

}