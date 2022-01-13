component extends = "org.lucee.cfml.test.LuceeTestCase" {


    function run( testResults, textbox ) {

        describe("testcase for LDEV-3736", function(){

            it(title="Arithmetic addition with empty string in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT 5+5 AS result,
                      ''+5 as result2,
                      5+'' as result3,
                      ''+'' as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 10 );
				expect( actual.result2 ).toBe( 5 );
				expect( actual.result3 ).toBe( 5 );
				expect( actual.result4 ).toBe( '' );
            });

            it(title="Arithmetic subtraction empty string in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT 20-10 AS result,
                      ''-5 as result2,
                      5-'' as result3,
                      ''-'' as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 10 );
				expect( actual.result2 ).toBe( -5 );
				expect( actual.result3 ).toBe( 5 );
				expect( actual.result4 ).toBe( 0 );
            });

            it(title="Arithmetic multiplication empty string in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT 2*5 AS result,
                      ''*5 as result2,
                      5*'' as result3,
                      ''*'' as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 10 );
				expect( actual.result2 ).toBe( 0 );
				expect( actual.result3 ).toBe( 0 );
				expect( actual.result4 ).toBe( 0 );
            });

            it(title="Arithmetic division empty string in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT 20/2 AS result,
                      ''/5 as result2
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 10 );
				expect( actual.result2 ).toBe( 0 );
            });

            it(title="Arithmetic bitwise empty string in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT 4^2 AS result,
                      ''^5 as result2,
                      5^'' as result3,
                      ''^'' as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 6 );
				expect( actual.result2 ).toBe( 5 );
				expect( actual.result3 ).toBe( 5 );
				expect( actual.result4 ).toBe( 0 );
            });

            it(title="Arithmetic modulus empty string in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                // Note % and mod() have different implemntations
                var actual = queryExecute(
                    "SELECT 21%11 AS result,
                      ''%5 as result2,
                      mod( 21, 11 ) AS result3,
                      mod( '', 5 ) as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 10 );
				expect( actual.result2 ).toBe( 0 );
				expect( actual.result3 ).toBe( 10 );
				expect( actual.result4 ).toBe( 0 );
            });

            it(title="Arithmetic exponent empty string in QoQ", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT power( 4, 2 ) AS result,
                      power( '', 5 ) as result2,
                      power( 5, '' ) as result3,
                      power( '', '' ) as result4
                    FROM qry",
                    [],
                    {dbtype="query"} );
				expect( actual.result ).toBe( 16 );
				expect( actual.result2 ).toBe( 0 );
				expect( actual.result3 ).toBe( 1 );
				expect( actual.result4 ).toBe( 1 );
            });

        });

    }

}