component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	private query function getTestData() {
		var qry = queryNew( "name", "varchar", [
			[ "Modica" ],
			[ "mod-lower" ],
			[ "Tabitha" ],
			[ "UPPER" ]
		] );
		return qry;
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6151 - QoQ case sensitivity with native engine", function() {

			it( title="LIKE is case-insensitive by default (backwards compat)", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name LIKE '%mod%'",
					{},
					{ dbtype: { type: "query", engine: "native" } }
				);
				expect( result.recordCount ).toBe( 2 );
			});

			it( title="LIKE is case-sensitive when caseSensitive=true", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name LIKE '%mod%'",
					{},
					{ dbtype: { type: "query", caseSensitive: true, engine: "native" } }
				);
				expect( result.recordCount ).toBe( 1 );
				expect( result.name ).toBe( "mod-lower" );
			});

			it( title="LIKE '%MOD%' case-sensitive matches nothing", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name LIKE '%MOD%'",
					{},
					{ dbtype: { type: "query", caseSensitive: true, engine: "native" } }
				);
				expect( result.recordCount ).toBe( 0 );
			});

			it( title="NOT LIKE case-sensitive", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name NOT LIKE '%MOD%'",
					{},
					{ dbtype: { type: "query", caseSensitive: true, engine: "native" } }
				);
				expect( result.recordCount ).toBe( 4 );
			});

			it( title="= is case-insensitive by default", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name = 'modica'",
					{},
					{ dbtype: { type: "query", engine: "native" } }
				);
				expect( result.recordCount ).toBe( 1 );
			});

			it( title="= is case-sensitive when caseSensitive=true", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name = 'modica'",
					{},
					{ dbtype: { type: "query", caseSensitive: true, engine: "native" } }
				);
				expect( result.recordCount ).toBe( 0 );
			});

			it( title="= exact case matches when caseSensitive=true", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name = 'Modica'",
					{},
					{ dbtype: { type: "query", caseSensitive: true, engine: "native" } }
				);
				expect( result.recordCount ).toBe( 1 );
			});

			it( title="<> case-sensitive", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name <> 'modica'",
					{},
					{ dbtype: { type: "query", caseSensitive: true, engine: "native" } }
				);
				expect( result.recordCount ).toBe( 4 );
			});

			it( title="IN is case-insensitive by default", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name IN ('modica','tabitha')",
					{},
					{ dbtype: { type: "query", engine: "native" } }
				);
				expect( result.recordCount ).toBe( 2 );
			});

			it( title="IN is case-sensitive when caseSensitive=true", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name IN ('modica','tabitha')",
					{},
					{ dbtype: { type: "query", caseSensitive: true, engine: "native" } }
				);
				expect( result.recordCount ).toBe( 0 );
			});

			it( title="IN exact case matches when caseSensitive=true", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name IN ('Modica','Tabitha')",
					{},
					{ dbtype: { type: "query", caseSensitive: true, engine: "native" } }
				);
				expect( result.recordCount ).toBe( 2 );
			});

			it( title="dbtype string still works for backwards compat", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name = 'modica'",
					{},
					{ dbtype: "query" }
				);
				expect( result.recordCount ).toBe( 1 );
			});

		});

		describe( "LDEV-6151 - QoQ case sensitivity with HSQLDB engine", function() {

			it( title="LIKE is case-insensitive by default with HSQLDB", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name LIKE '%mod%'",
					{},
					{ dbtype: { type: "query", engine: "hsqldb" } }
				);
				expect( result.recordCount ).toBe( 2 );
			});

			it( title="LIKE is case-sensitive with HSQLDB when caseSensitive=true", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name LIKE '%mod%'",
					{},
					{ dbtype: { type: "query", caseSensitive: true, engine: "hsqldb" } }
				);
				expect( result.recordCount ).toBe( 1 );
				expect( result.name ).toBe( "mod-lower" );
			});

			it( title="= is case-sensitive with HSQLDB when caseSensitive=true", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name = 'modica'",
					{},
					{ dbtype: { type: "query", caseSensitive: true, engine: "hsqldb" } }
				);
				expect( result.recordCount ).toBe( 0 );
			});

			it( title="IN is case-sensitive with HSQLDB when caseSensitive=true", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry WHERE name IN ('modica','tabitha')",
					{},
					{ dbtype: { type: "query", caseSensitive: true, engine: "hsqldb" } }
				);
				expect( result.recordCount ).toBe( 0 );
			});

		});

		describe( "LDEV-6151 - engine option", function() {

			it( title="engine=native works for simple queries", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry ORDER BY name",
					{},
					{ dbtype: { type: "query", engine: "native" } }
				);
				expect( result.recordCount ).toBe( 4 );
			});

			it( title="engine=hsqldb works for simple queries", body=function( currentSpec ) {
				var qry = getTestData();
				var result = queryExecute(
					"SELECT name FROM qry ORDER BY name",
					{},
					{ dbtype: { type: "query", engine: "hsqldb" } }
				);
				expect( result.recordCount ).toBe( 4 );
			});

		});

		describe( "LDEV-6151 - struct dbtype with INNER JOIN falls back to HSQLDB", function() {

			it( title="INNER JOIN works with struct dbtype and no engine specified", body=function( currentSpec ) {
				var t1 = queryNew( "id,val", "integer,double", [ [ 1, 10 ] ] );
				var t2 = queryNew( "id,name", "integer,varchar", [ [ 1, "A" ] ] );
				var result = queryExecute(
					"SELECT * FROM t1 INNER JOIN t2 ON t1.id = t2.id",
					{},
					{ dbtype: { type: "query" } }
				);
				expect( result.recordCount ).toBe( 1 );
			});

			it( title="INNER JOIN works with string dbtype", body=function( currentSpec ) {
				var t1 = queryNew( "id,val", "integer,double", [ [ 1, 10 ] ] );
				var t2 = queryNew( "id,name", "integer,varchar", [ [ 1, "A" ] ] );
				var result = queryExecute(
					"SELECT * FROM t1 INNER JOIN t2 ON t1.id = t2.id",
					{},
					{ dbtype: "query" }
				);
				expect( result.recordCount ).toBe( 1 );
			});

			it( title="INNER JOIN works with explicit engine=auto", body=function( currentSpec ) {
				var t1 = queryNew( "id,val", "integer,double", [ [ 1, 10 ] ] );
				var t2 = queryNew( "id,name", "integer,varchar", [ [ 1, "A" ] ] );
				var result = queryExecute(
					"SELECT * FROM t1 INNER JOIN t2 ON t1.id = t2.id",
					{},
					{ dbtype: { type: "query", engine: "auto" } }
				);
				expect( result.recordCount ).toBe( 1 );
			});

		});
	}
}
