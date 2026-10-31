component extends="org.lucee.cfml.test.LuceeTestCase" labels="query,h2" {

	function beforeAll() {
		variables.ds = server.getDatasource( "h2", server._getTempDir( "LDEV5026" ) );
		queryExecute( "CREATE TABLE IF NOT EXISTS t5026 ( id int, name varchar(20) )", {}, { datasource: variables.ds } );
	}

	function afterAll() {
		queryExecute( "DROP TABLE IF EXISTS t5026", {}, { datasource: variables.ds } );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-5026 - option to leave sql and sqlparameters out of the query result struct", function() {

			it( title="default: result contains sql and sqlparameters", body=function( currentSpec ) {
				queryExecute(
					"INSERT INTO t5026 VALUES ( :id, :name )",
					{ id: 1, name: "a" },
					{ datasource: variables.ds, result: "local.res" }
				);
				expect( res ).toHaveKey( "sql" );
				expect( res ).toHaveKey( "sqlparameters" );
				expect( res.sqlparameters ).toBeArray();
				expect( res.sqlparameters ).toHaveLength( 2 );
			});

			it( title="the stored sql contains ? placeholders, not the parameter values", body=function( currentSpec ) {
				queryExecute(
					"INSERT INTO t5026 VALUES ( :id, :name )",
					{ id: 2, name: "secret-value-5026" },
					{ datasource: variables.ds, result: "local.res" }
				);
				expect( res.sql ).toInclude( "?" );
				expect( res.sql ).notToInclude( "secret-value-5026" );
				expect( res.sqlparameters[ 2 ] ).toBe( "secret-value-5026" );
			});

			it( title="queryExecute includeSQL=false leaves sql and sqlparameters out", body=function( currentSpec ) {
				queryExecute(
					"INSERT INTO t5026 VALUES ( :id, :name )",
					{ id: 3, name: "c" },
					{ datasource: variables.ds, result: "local.res", includeSQL: false }
				);
				expect( res ).notToHaveKey( "sql" );
				expect( res ).notToHaveKey( "sqlparameters" );
				expect( res ).toHaveKey( "recordcount" );
				expect( res ).toHaveKey( "executionTime" );
				expect( res ).toHaveKey( "cached" );
			});

			it( title="queryExecute includeSQL=true keeps sql and sqlparameters", body=function( currentSpec ) {
				queryExecute(
					"INSERT INTO t5026 VALUES ( :id, :name )",
					{ id: 4, name: "d" },
					{ datasource: variables.ds, result: "local.res", includeSQL: true }
				);
				expect( res ).toHaveKey( "sql" );
				expect( res ).toHaveKey( "sqlparameters" );
			});

			it( title="tag cfquery includeSQL=false on a select, query and other result keys are intact", body=function( currentSpec ) {
				query name="local.qry" result="local.res" datasource=variables.ds includeSQL=false {
					echo( "SELECT id, name FROM t5026 WHERE id = " );
					queryparam value=1 cfsqltype="integer";
				}
				expect( qry.recordCount ).toBe( 1 );
				expect( qry.name ).toBe( "a" );
				expect( res ).notToHaveKey( "sql" );
				expect( res ).notToHaveKey( "sqlparameters" );
				expect( res.recordcount ).toBe( 1 );
				expect( res.columnlist ).toBe( "id,name" );
			});

			it( title="tag cfquery without includeSQL keeps sql and sqlparameters", body=function( currentSpec ) {
				query name="local.qry" result="local.res" datasource=variables.ds {
					echo( "SELECT id, name FROM t5026 WHERE id = " );
					queryparam value=1 cfsqltype="integer";
				}
				expect( res ).toHaveKey( "sql" );
				expect( res.sql ).toInclude( "?" );
				expect( res.sqlparameters ).toHaveLength( 1 );
			});

			it( title="includeSQL=false with a query without parameters", body=function( currentSpec ) {
				queryExecute( "SELECT id FROM t5026", {}, { datasource: variables.ds, result: "local.res", includeSQL: false } );
				expect( res ).notToHaveKey( "sql" );
				expect( res ).notToHaveKey( "sqlparameters" );
				expect( res.recordcount ).toBeGTE( 1 );
			});

			it( title="includeSQL=false with a cached query", body=function( currentSpec ) {
				var sql = "SELECT id FROM t5026 WHERE id = :id";
				var opts = { datasource: variables.ds, result: "local.res", includeSQL: false, cachedWithin: createTimespan( 0, 0, 1, 0 ) };
				queryExecute( sql, { id: 1 }, opts );
				queryExecute( sql, { id: 1 }, opts );
				expect( res.cached ).toBeTrue();
				expect( res ).notToHaveKey( "sql" );
				expect( res ).notToHaveKey( "sqlparameters" );
			});

			it( title="includeSQL=false on an insert still returns the generated key", body=function( currentSpec ) {
				queryExecute( "CREATE TABLE IF NOT EXISTS t5026gk ( id int auto_increment primary key, name varchar(20) )", {}, { datasource: variables.ds } );
				try {
					queryExecute(
						"INSERT INTO t5026gk ( name ) VALUES ( :name )",
						{ name: "x" },
						{ datasource: variables.ds, result: "local.res", includeSQL: false }
					);
					expect( res ).notToHaveKey( "sql" );
					expect( res ).toHaveKey( "generatedKey" );
					expect( res.generatedKey ).toBe( 1 );
				}
				finally {
					queryExecute( "DROP TABLE IF EXISTS t5026gk", {}, { datasource: variables.ds } );
				}
			});

			it( title="this.query.includeSQL=false in Application.cfc, per query attribute overrides it", body=function( currentSpec ) {
				var result = _internalRequest( template: createURI( "LDEV5026/appDefault.cfm" ) );
				expect( result.status ).toBe( 200 );
				var data = deserializeJSON( result.fileContent );

				expect( data.r1.hasSql ).toBeFalse();
				expect( data.r1.hasParams ).toBeFalse();
				expect( data.r1.recordcount ).toBe( 1 );

				expect( data.r2.hasSql ).toBeTrue();
				expect( data.r2.hasParams ).toBeTrue();
				expect( data.r2.sql ).toInclude( "?" );
				expect( data.r2.sqlparameters ).toHaveLength( 2 );

				expect( data.r3.hasSql ).toBeFalse();
				expect( data.r3.hasParams ).toBeFalse();
				expect( data.r3.recordcount ).toBe( 1 );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
