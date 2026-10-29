component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll(){
		variables.preciseMath = getApplicationSettings().preciseMath;
		variables.big = "20150817112756892";
		variables.src = queryNew( "id,v", "integer,varchar", [ [ 1, variables.big ], [ 2, "1" ] ] );
	}

	function afterAll(){
		application action="update" preciseMath=variables.preciseMath;
	}

	function run( testResults, testBox ){
		describe( "LDEV-0496 large numbers in query of queries", function(){

			loop list="false,true" item="local.mode" {
				describe( title="preciseMath=#mode#", body=function(){

					it( title="cf_sql_numeric param keeps all digits", data={ mode: mode }, body=function( data ){
						application action="update" preciseMath=data.mode;
						var q = queryExecute(
							"SELECT ? AS n FROM src WHERE id = 1",
							[ { value: big, cfsqltype: "cf_sql_numeric" } ],
							{ dbtype: "query" }
						);
						expect( toString( q.n ) ).toBe( big );
					} );

					it( title="cf_sql_decimal param keeps all digits", data={ mode: mode }, body=function( data ){
						application action="update" preciseMath=data.mode;
						var q = queryExecute(
							"SELECT ? AS n FROM src WHERE id = 1",
							[ { value: big, cfsqltype: "cf_sql_decimal" } ],
							{ dbtype: "query" }
						);
						expect( toString( q.n ) ).toBe( big );
					} );

					it( title="cf_sql_bigint param keeps all digits", data={ mode: mode }, body=function( data ){
						application action="update" preciseMath=data.mode;
						var q = queryExecute(
							"SELECT ? AS n FROM src WHERE id = 1",
							[ { value: big, cfsqltype: "cf_sql_bigint" } ],
							{ dbtype: "query" }
						);
						expect( toString( q.n ) ).toBe( big );
					} );

					it( title="inline literal keeps all digits", data={ mode: mode }, body=function( data ){
						application action="update" preciseMath=data.mode;
						var q = queryExecute( "SELECT #big# AS n FROM src WHERE id = 1", [], { dbtype: "query" } );
						expect( toString( q.n ) ).toBe( big );
					} );

					it( title="the example from the ticket", data={ mode: mode }, body=function( data ){
						application action="update" preciseMath=data.mode;
						var q = queryExecute(
							"SELECT ? AS num, #big# AS inline FROM src WHERE id = 1",
							[ { value: big, cfsqltype: "cf_sql_numeric" } ],
							{ dbtype: "query" }
						);
						expect( toString( q.num ) ).toBe( big );
						expect( toString( q.inline ) ).toBe( big );
					} );

					it( title="varchar column + 0 keeps all digits", data={ mode: mode }, body=function( data ){
						application action="update" preciseMath=data.mode;
						var q = queryExecute( "SELECT v + 0 AS n FROM src WHERE id = 1", [], { dbtype: "query" } );
						expect( toString( q.n ) ).toBe( big );
					} );

					it( title="WHERE varchar column = numeric param matches the same number", data={ mode: mode }, body=function( data ){
						application action="update" preciseMath=data.mode;
						var q = queryExecute(
							"SELECT id FROM src WHERE v = ?",
							[ { value: big, cfsqltype: "cf_sql_numeric" } ],
							{ dbtype: "query" }
						);
						expect( q.recordCount ).toBe( 1 );
						expect( q.id ).toBe( 1 );
					} );

					it( title="WHERE varchar column = numeric param does not match a different number", data={ mode: mode }, body=function( data ){
						application action="update" preciseMath=data.mode;
						var q = queryExecute(
							"SELECT id FROM src WHERE v = ?",
							[ { value: "20150817112756888", cfsqltype: "cf_sql_numeric" } ],
							{ dbtype: "query" }
						);
						expect( q.recordCount ).toBe( 0 );
					} );

					it( title="WHERE varchar column = inline literal", data={ mode: mode }, body=function( data ){
						application action="update" preciseMath=data.mode;
						var q = queryExecute( "SELECT id FROM src WHERE v = #big#", [], { dbtype: "query" } );
						expect( q.recordCount ).toBe( 1 );
						expect( q.id ).toBe( 1 );
					} );

					it( title="small numbers are not affected", data={ mode: mode }, body=function( data ){
						application action="update" preciseMath=data.mode;
						var q = queryExecute(
							"SELECT ? AS a, 12.5 AS b, v + 1 AS c, -? AS d FROM src WHERE id = 2",
							[ { value: "100", cfsqltype: "cf_sql_numeric" }, { value: "2.5", cfsqltype: "cf_sql_decimal" } ],
							{ dbtype: "query" }
						);
						expect( q.a ).toBe( 100 );
						expect( q.b ).toBe( 12.5 );
						expect( q.c ).toBe( 2 );
						expect( q.d ).toBe( -2.5 );
					} );

				} );
			}

			describe( "preciseMath=true does exact arithmetic with large numbers", function(){

				beforeEach( function(){
					application action="update" preciseMath=true;
				} );

				it( title="plus", body=function( currentSpec ){
					var q = queryExecute(
						"SELECT ? + 1 AS a, v + 1 AS b, #big# + 1 AS c FROM src WHERE id = 1",
						[ { value: big, cfsqltype: "cf_sql_numeric" } ],
						{ dbtype: "query" }
					);
					expect( toString( q.a ) ).toBe( "20150817112756893" );
					expect( toString( q.b ) ).toBe( "20150817112756893" );
					expect( toString( q.c ) ).toBe( "20150817112756893" );
				} );

				it( title="minus", body=function( currentSpec ){
					var q = queryExecute(
						"SELECT ? - 1 AS a, v - 1 AS b, #big# - 1 AS c FROM src WHERE id = 1",
						[ { value: big, cfsqltype: "cf_sql_decimal" } ],
						{ dbtype: "query" }
					);
					expect( toString( q.a ) ).toBe( "20150817112756891" );
					expect( toString( q.b ) ).toBe( "20150817112756891" );
					expect( toString( q.c ) ).toBe( "20150817112756891" );
				} );

				it( title="multiply", body=function( currentSpec ){
					var q = queryExecute( "SELECT v * 3 AS a, #big# * 3 AS b FROM src WHERE id = 1", [], { dbtype: "query" } );
					expect( toString( q.a ) ).toBe( "60452451338270676" );
					expect( toString( q.b ) ).toBe( "60452451338270676" );
				} );

				it( title="divide", body=function( currentSpec ){
					var q = queryExecute( "SELECT v / 4 AS a, #big# / 4 AS b FROM src WHERE id = 1", [], { dbtype: "query" } );
					expect( toString( q.a ) ).toBe( "5037704278189223" );
					expect( toString( q.b ) ).toBe( "5037704278189223" );
				} );

				it( title="mod", body=function( currentSpec ){
					var q = queryExecute( "SELECT v % 10 AS a, MOD( v, 10 ) AS b, #big# % 10 AS c FROM src WHERE id = 1", [], { dbtype: "query" } );
					expect( q.a ).toBe( 2 );
					expect( q.b ).toBe( 2 );
					expect( q.c ).toBe( 2 );
				} );

				it( title="negate", body=function( currentSpec ){
					var q = queryExecute(
						"SELECT -? AS a FROM src WHERE id = 1",
						[ { value: big, cfsqltype: "cf_sql_numeric" } ],
						{ dbtype: "query" }
					);
					expect( toString( q.a ) ).toBe( "-" & big );
				} );

				it( title="divide by zero is still rejected", body=function( currentSpec ){
					expect( function(){
						queryExecute( "SELECT v / 0 AS a FROM src WHERE id = 1", [], { dbtype: "query" } );
					} ).toThrow();
					expect( function(){
						queryExecute( "SELECT v % 0 AS a FROM src WHERE id = 1", [], { dbtype: "query" } );
					} ).toThrow();
				} );

			} );

		} );
	}

}
