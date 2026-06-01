component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq"{

    function beforeAll() {
        variables.originalNS = getApplicationSettings().nullSupport;
    }

    function afterAll() {
        application action="update" NULLSupport=variables.originalNS;
    }

    private function withNS( required boolean enabled, required function fn ) {
        application action="update" NULLSupport=arguments.enabled;
        try { arguments.fn(); }
        finally { application action="update" NULLSupport=variables.originalNS; }
    }

    // 3 rows: (1, 10, 5), (2, null, null), (3, null, 7)
    // Lets us hit single-cell-null and both-cell-null branches in the same dataset.
    private query function getMixedNullsQry() {
        var q = queryNew( "id,amount,bonus", "integer,integer,integer" );
        queryAddRow( q );
        querySetCell( q, "id", 1 );
        querySetCell( q, "amount", 10 );
        querySetCell( q, "bonus", 5 );
        queryAddRow( q );
        querySetCell( q, "id", 2 );
        // amount and bonus NULL via "didn't set"
        queryAddRow( q );
        querySetCell( q, "id", 3 );
        querySetCell( q, "amount", javaCast( "null", "" ) );
        querySetCell( q, "bonus", 7 );
        return q;
    }

    function run( testResults, textbox ) {

        describe(title="testcase for LDEV-3734", body=function(){

            // Existing literal-NULL coverage -- runs under both FNS modes now
            // (was previously FNS=on only via blanket beforeAll, which is what
            //  let LDEV-6310 regress on the stored-cell path unnoticed).
            //
            // Null assertions use isNull( qry.getColumn(name).get(1,nullValue()) )
            // -- the Java-direct read is FNS-agnostic. CFML-side qry.colname
            // coerces null->"" under FNS=off so the .toBeNull() shorthand only
            // works under FNS=on.
            [ false, true ].each( function( fns ) {

                it(title="Arithmetic addition with literal NULL in QoQ -- FNS=#fns#", body=function( currentSpec ){
                    withNS( fns, function() {
                        var qry = QueryNew('foo','integer',[[40]]);
                        var actual = queryExecute(
                            "SELECT 5+5 AS result,
                              NULL+5 as result2,
                              5+NULL as result3,
                              NULL+NULL as result4
                            FROM qry",
                            [],
                            {dbtype="query"}
                        );
                        expect( actual.result ).toBe( 10 );
                        expect( isNull( actual.getColumn( 'result2' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result3' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result4' ).get( 1, nullValue() ) ) ).toBeTrue();
                    });
                });

                it(title="Arithmetic subtraction with literal NULL in QoQ -- FNS=#fns#", body=function( currentSpec ){
                    withNS( fns, function() {
                        var qry = QueryNew('foo','integer',[[40]]);
                        var actual = queryExecute(
                            "SELECT 20-10 AS result,
                              NULL-5 as result2,
                              5-NULL as result3,
                              NULL-NULL as result4
                            FROM qry",
                            [],
                            {dbtype="query"}
                        );
                        expect( actual.result ).toBe( 10 );
                        expect( isNull( actual.getColumn( 'result2' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result3' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result4' ).get( 1, nullValue() ) ) ).toBeTrue();
                    });
                });

                it(title="Arithmetic multiplication with literal NULL in QoQ -- FNS=#fns#", body=function( currentSpec ){
                    withNS( fns, function() {
                        var qry = QueryNew('foo','integer',[[40]]);
                        var actual = queryExecute(
                            "SELECT 2*5 AS result,
                              NULL*5 as result2,
                              5*NULL as result3,
                              NULL*NULL as result4
                            FROM qry",
                            [],
                            {dbtype="query"}
                          );
                        expect( actual.result ).toBe( 10 );
                        expect( isNull( actual.getColumn( 'result2' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result3' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result4' ).get( 1, nullValue() ) ) ).toBeTrue();
                    });
                });

                it(title="Arithmetic division with literal NULL in QoQ -- FNS=#fns#", body=function( currentSpec ){
                    withNS( fns, function() {
                        var qry = QueryNew('foo','integer',[[40]]);
                        var actual = queryExecute(
                            "SELECT 20/2 AS result,
                              NULL/5 as result2,
                              5/NULL as result3,
                              NULL/NULL as result4
                            FROM qry",
                            [],
                            {dbtype="query"}
                        );
                        expect( actual.result ).toBe( 10 );
                        expect( isNull( actual.getColumn( 'result2' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result3' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result4' ).get( 1, nullValue() ) ) ).toBeTrue();
                    });
                });

                it(title="Arithmetic bitwise with literal NULL in QoQ -- FNS=#fns#", body=function( currentSpec ){
                    withNS( fns, function() {
                        var qry = QueryNew('foo','integer',[[40]]);
                        var actual = queryExecute(
                            "SELECT 4^2 AS result,
                              NULL^5 as result2,
                              5^NULL as result3,
                              NULL^NULL as result4
                            FROM qry",
                            [],
                            {dbtype="query"}
                        );
                        expect( actual.result ).toBe( 6 );
                        expect( isNull( actual.getColumn( 'result2' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result3' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result4' ).get( 1, nullValue() ) ) ).toBeTrue();
                    });
                });

                it(title="Arithmetic modulus with literal NULL in QoQ -- FNS=#fns#", body=function( currentSpec ){
                    withNS( fns, function() {
                        var qry = QueryNew('foo','integer',[[40]]);
                        // Note % and mod() have different implementations
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
                            {dbtype="query"}
                        );
                        expect( actual.result ).toBe( 10 );
                        expect( isNull( actual.getColumn( 'result2' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result3' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result4' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( actual.result5 ).toBe( 10 );
                        expect( isNull( actual.getColumn( 'result6' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result7' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result8' ).get( 1, nullValue() ) ) ).toBeTrue();
                    });
                });

                it(title="Arithmetic exponent with literal NULL in QoQ -- FNS=#fns#", body=function( currentSpec ){
                    withNS( fns, function() {
                        var qry = QueryNew('foo','integer',[[40]]);
                        var actual = queryExecute(
                            "SELECT power( 4, 2 ) AS result,
                              power( NULL, 5 ) as result2,
                              power( 5, NULL ) as result3,
                              power( NULL, NULL ) as result4
                            FROM qry",
                            [],
                            {dbtype="query"} );
                        expect( actual.result ).toBe( 16 );
                        expect( isNull( actual.getColumn( 'result2' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result3' ).get( 1, nullValue() ) ) ).toBeTrue();
                        expect( isNull( actual.getColumn( 'result4' ).get( 1, nullValue() ) ) ).toBeTrue();
                    });
                });

                it(title="Arithmetic operation with literal NULL in QoQ -- FNS=#fns#", body=function( currentSpec ){
                    withNS( fns, function() {
                        var qry = QueryNew('foo','integer',[[40]]);
                        var actual = queryExecute("SELECT NULL-5 AS inf FROM qry", {}, {dbtype="query"});
                        expect( isNull( actual.getColumn( 'inf' ).get( 1, nullValue() ) ) ).toBeTrue();
                    });
                });

            });

            // LDEV-6310 -- stored-null cell coverage.
            // The literal-NULL specs above route through Value and bypass
            // executeColumn entirely. The regressed path is stored-null cell
            // -> column.getValue -> arithmetic operator. Each spec witnesses
            // null preservation via SQL-side IS NULL filter (counts and id
            // partitions) so the expression evaluator path is actually
            // traversed end-to-end. Skips * (separate SQLPrettyfier bug).
            [ false, true ].each( function( fns ) {

                it(title="(amount + 1) IS NULL on stored-null cells -- FNS=#fns#", body=function() {
                    withNS( fns, function() {
                        var q = getMixedNullsQry();
                        var r = queryExecute(
                            "SELECT id FROM q WHERE (amount + 1) IS NULL ORDER BY id",
                            {},
                            {dbtype="query"}
                        );
                        expect( valueList( r.id ) ).toBe( "2,3" );
                    });
                });

                it(title="(amount - 5) IS NULL on stored-null cells -- FNS=#fns#", body=function() {
                    withNS( fns, function() {
                        var q = getMixedNullsQry();
                        var r = queryExecute(
                            "SELECT id FROM q WHERE (amount - 5) IS NULL ORDER BY id",
                            {},
                            {dbtype="query"}
                        );
                        expect( valueList( r.id ) ).toBe( "2,3" );
                    });
                });

                it(title="(amount / 2) IS NULL on stored-null cells -- FNS=#fns#", body=function() {
                    withNS( fns, function() {
                        var q = getMixedNullsQry();
                        var r = queryExecute(
                            "SELECT id FROM q WHERE (amount / 2) IS NULL ORDER BY id",
                            {},
                            {dbtype="query"}
                        );
                        expect( valueList( r.id ) ).toBe( "2,3" );
                    });
                });

                it(title="(amount ^ 2) IS NULL on stored-null cells -- FNS=#fns#", body=function() {
                    withNS( fns, function() {
                        var q = getMixedNullsQry();
                        var r = queryExecute(
                            "SELECT id FROM q WHERE (amount ^ 2) IS NULL ORDER BY id",
                            {},
                            {dbtype="query"}
                        );
                        expect( valueList( r.id ) ).toBe( "2,3" );
                    });
                });

                it(title="(amount % 5) IS NULL on stored-null cells -- FNS=#fns#", body=function() {
                    withNS( fns, function() {
                        var q = getMixedNullsQry();
                        var r = queryExecute(
                            "SELECT id FROM q WHERE (amount % 5) IS NULL ORDER BY id",
                            {},
                            {dbtype="query"}
                        );
                        expect( valueList( r.id ) ).toBe( "2,3" );
                    });
                });

                it(title="mod( amount, 5 ) IS NULL on stored-null cells -- FNS=#fns#", body=function() {
                    withNS( fns, function() {
                        var q = getMixedNullsQry();
                        var r = queryExecute(
                            "SELECT id FROM q WHERE mod( amount, 5 ) IS NULL ORDER BY id",
                            {},
                            {dbtype="query"}
                        );
                        expect( valueList( r.id ) ).toBe( "2,3" );
                    });
                });

                it(title="power( amount, 2 ) IS NULL on stored-null cells -- FNS=#fns#", body=function() {
                    withNS( fns, function() {
                        var q = getMixedNullsQry();
                        var r = queryExecute(
                            "SELECT id FROM q WHERE power( amount, 2 ) IS NULL ORDER BY id",
                            {},
                            {dbtype="query"}
                        );
                        expect( valueList( r.id ) ).toBe( "2,3" );
                    });
                });

                it(title="(amount + bonus) IS NULL covers single-cell-null AND both-cell-null -- FNS=#fns#", body=function() {
                    withNS( fns, function() {
                        var q = getMixedNullsQry();
                        // row 2: both null (null + null = null)
                        // row 3: only amount null (null + 7 = null)
                        var r = queryExecute(
                            "SELECT id FROM q WHERE (amount + bonus) IS NULL ORDER BY id",
                            {},
                            {dbtype="query"}
                        );
                        expect( valueList( r.id ) ).toBe( "2,3" );
                    });
                });

                it(title="non-null row still computes correctly through the same path -- FNS=#fns#", body=function() {
                    withNS( fns, function() {
                        var q = getMixedNullsQry();
                        var r = queryExecute(
                            "SELECT id FROM q WHERE (amount + 1) = 11",
                            {},
                            {dbtype="query"}
                        );
                        expect( valueList( r.id ) ).toBe( "1" );
                    });
                });

                it(title="IS NOT NULL only matches the non-null row -- FNS=#fns#", body=function() {
                    withNS( fns, function() {
                        var q = getMixedNullsQry();
                        var r = queryExecute(
                            "SELECT id FROM q WHERE (amount - 5) IS NOT NULL",
                            {},
                            {dbtype="query"}
                        );
                        expect( valueList( r.id ) ).toBe( "1" );
                    });
                });

            });

        });

    }

}
