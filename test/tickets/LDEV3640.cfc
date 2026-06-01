component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

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

	// 3 rows: (1, 10, 5) non-null, (2, null, null) both null, (3, null, 7) amount null only
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

	function run( testResults , testBox ) {

		describe( 'QofQ nulls' , function(){

			it( 'Stay null when unioned' , function() {
				var qs_result = queryNew("col" , "int", [1]);
				```
				<cfquery name="qs_result" dbtype="query">
						select null as test
						from qs_result
						union
						select null as test
						from qs_result
				</cfquery>
				```

				expect( isNull( qs_result.getColumn('test').get(1,nullValue()) ) ).toBeTrue();
			});

			it( 'Stay null when grouped' , function() {
				var qs_result = queryNew("col,col2" , "string,string", [
					['test',nullValue()],
					['foo',nullValue()],
					['test',nullValue()]
				]);
				```
				<cfquery name="qs_result" dbtype="query">
					select col,col2,isnull(col2,42) as test
					from qs_result
					group by col,col2
				</cfquery>
				```

				expect( isNull( qs_result.getColumn('col2').get(1,nullValue()) ) ).toBeTrue();
				expect( qs_result.test ).toBe( 42 );

			});

			it( 'Stay null when aggregated' , function() {

				var qs_result = queryNew("dnum_auto,amount_local" , "integer,integer", [[10,10],[20,20]]);
				```
				<cfquery name="local.testquery" dbtype="query">
					select sum(amount_local) as amount_local
					from qs_result
					where dnum_auto = 1000
				</cfquery>
				```

				expect( testquery.recordCount ).toBe( 1 );
				expect( testquery.amount_local ).toBe( '' );
				expect( isNull( testquery.getColumn('amount_local').get(1,nullValue()) ) ).toBeTrue();

				```
				<cfquery name="local.testquery2" dbtype="query">
					select sum(amount_local) as amount_local
					from testquery
				</cfquery>
				```

				expect( testquery2.recordCount ).toBe( 1 );
				expect( testquery2.amount_local ).toBe( '' );
				expect( isNull( testquery2.getColumn('amount_local').get(1,nullValue()) ) ).toBeTrue();
			});

		});

		// LDEV-6310 -- regression coverage. The contract is "preserve nulls
		// internally regardless of full null support" -- the original LDEV-3640
		// tests above only validate cell storage via Java-direct getColumn().get().
		// These specs exercise stored-null cells through the QoQ expression
		// evaluator (executeColumn -> arithmetic operator -> WHERE/IS NULL),
		// which is the path that regressed in QoQ.java:1494.
		describe( 'LDEV-6310 stored-null cells preserve null through expression evaluator' , function() {

			[ false, true ].each( function( fns ) {

				it( title="amount IS NULL control rows 2,3 -- FNS=#fns#", body=function() {
					withNS( fns, function() {
						var q = getMixedNullsQry();
						var r = queryExecute(
							"SELECT id FROM q WHERE amount IS NULL ORDER BY id",
							{},
							{ dbtype: "query" }
						);
						expect( valueList( r.id ) ).toBe( "2,3" );
					});
				});

				it( title="(amount + 1) IS NULL preserves null on stored-null rows -- FNS=#fns#", body=function() {
					withNS( fns, function() {
						var q = getMixedNullsQry();
						var r = queryExecute(
							"SELECT id FROM q WHERE (amount + 1) IS NULL ORDER BY id",
							{},
							{ dbtype: "query" }
						);
						expect( valueList( r.id ) ).toBe( "2,3" );
					});
				});

				it( title="(1 + amount) IS NULL -- literal-on-left, column-on-right -- FNS=#fns#", body=function() {
					withNS( fns, function() {
						var q = getMixedNullsQry();
						var r = queryExecute(
							"SELECT id FROM q WHERE (1 + amount) IS NULL ORDER BY id",
							{},
							{ dbtype: "query" }
						);
						expect( valueList( r.id ) ).toBe( "2,3" );
					});
				});

				it( title="(amount - 5) IS NULL -- FNS=#fns#", body=function() {
					withNS( fns, function() {
						var q = getMixedNullsQry();
						var r = queryExecute(
							"SELECT id FROM q WHERE (amount - 5) IS NULL ORDER BY id",
							{},
							{ dbtype: "query" }
						);
						expect( valueList( r.id ) ).toBe( "2,3" );
					});
				});

				it( title="(amount / 2) IS NULL -- FNS=#fns#", body=function() {
					withNS( fns, function() {
						var q = getMixedNullsQry();
						var r = queryExecute(
							"SELECT id FROM q WHERE (amount / 2) IS NULL ORDER BY id",
							{},
							{ dbtype: "query" }
						);
						expect( valueList( r.id ) ).toBe( "2,3" );
					});
				});

				it( title="(amount + bonus) IS NULL -- two stored-null cells, partial and both-null -- FNS=#fns#", body=function() {
					withNS( fns, function() {
						var q = getMixedNullsQry();
						var r = queryExecute(
							"SELECT id FROM q WHERE (amount + bonus) IS NULL ORDER BY id",
							{},
							{ dbtype: "query" }
						);
						expect( valueList( r.id ) ).toBe( "2,3" );
					});
				});

				it( title="non-null row still computes correctly -- (amount + 1) = 11 -- FNS=#fns#", body=function() {
					withNS( fns, function() {
						var q = getMixedNullsQry();
						var r = queryExecute(
							"SELECT id FROM q WHERE (amount + 1) = 11",
							{},
							{ dbtype: "query" }
						);
						expect( valueList( r.id ) ).toBe( "1" );
					});
				});

				it( title="IS NOT NULL only matches the non-null row -- FNS=#fns#", body=function() {
					withNS( fns, function() {
						var q = getMixedNullsQry();
						var r = queryExecute(
							"SELECT id FROM q WHERE (amount + 1) IS NOT NULL",
							{},
							{ dbtype: "query" }
						);
						expect( valueList( r.id ) ).toBe( "1" );
					});
				});

				it( title="projected expression preserves null in result column -- FNS=#fns#", body=function() {
					withNS( fns, function() {
						var q = getMixedNullsQry();
						var r = queryExecute(
							"SELECT id, (amount + 1) AS r FROM q WHERE id = 3",
							{},
							{ dbtype: "query" }
						);
						expect( r.recordcount ).toBe( 1 );
						expect( isNull( r.getColumn( 'r' ).get( 1, nullValue() ) ) ).toBeTrue( "projected (null amount + 1) should be null in output column" );
					});
				});

			});
		});

	}


}