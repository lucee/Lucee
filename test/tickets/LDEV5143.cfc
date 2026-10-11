component extends="org.lucee.cfml.test.LuceeTestCase" labels="postgres" {

	function beforeAll() {
		variables.pg = server.getDatasource( "postgres" );
		variables.prefix = "ldev5143_" & left( lcase( hash( createUniqueID() ) ), 8 );
		variables.mixed = "Ldev5143_" & right( prefix, 8 ) & "_Mixed";
		variables.plain = prefix & "_plain";
		if ( !notHasPostgres() ) {
			dropTables();
			queryExecute( 'CREATE TABLE "#mixed#" ( "Id" INT PRIMARY KEY, "UserName" VARCHAR(50) )', {}, { datasource: pg } );
			queryExecute( 'CREATE INDEX "#mixed#_idx" ON "#mixed#" ( "UserName" )', {}, { datasource: pg } );
			queryExecute( 'CREATE TABLE #plain# ( id INT PRIMARY KEY, name VARCHAR(50) )', {}, { datasource: pg } );
		}
	}

	function afterAll() {
		if ( !notHasPostgres() ) dropTables();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5143 cfdbinfo with quoted mixed case PostgreSQL tables", function() {

			it( title="columns of a quoted mixed case table", skip=notHasPostgres(), body=function() {
				dbinfo type="columns" table=variables.mixed datasource=variables.pg name="local.cols";
				expect( cols.recordCount ).toBe( 2 );
				expect( valueList( cols.column_name ) ).toBe( "Id,UserName" );
			});

			it( title="columns of a quoted mixed case table with schema", skip=notHasPostgres(), body=function() {
				dbinfo type="columns" table="public.#variables.mixed#" datasource=variables.pg name="local.cols";
				expect( cols.recordCount ).toBe( 2 );
			});

			it( title="index of a quoted mixed case table", skip=notHasPostgres(), body=function() {
				dbinfo type="index" table=variables.mixed datasource=variables.pg name="local.idx";
				expect( valueList( idx.index_name ) ).toInclude( "#variables.mixed#_idx" );
			});

			it( title="foreignkeys of a quoted mixed case table", skip=notHasPostgres(), body=function() {
				dbinfo type="foreignkeys" table=variables.mixed datasource=variables.pg name="local.fk";
				expect( fk.recordCount ).toBe( 0 );
			});

			it( title="an unquoted table can still be found with any case", skip=notHasPostgres(), body=function() {
				dbinfo type="columns" table=ucase( variables.plain ) datasource=variables.pg name="local.cols";
				expect( cols.recordCount ).toBe( 2 );
			});

			it( title="a missing table still throws", skip=notHasPostgres(), body=function() {
				expect( function() {
					dbinfo type="columns" table="#variables.prefix#_missing" datasource=variables.pg name="local.cols";
				}).toThrow();
			});

		});
	}

	private boolean function notHasPostgres() {
		return structCount( server.getDatasource( "postgres" ) ) == 0;
	}

	private void function dropTables() {
		queryExecute( 'DROP TABLE IF EXISTS "#variables.mixed#"', {}, { datasource: variables.pg } );
		queryExecute( 'DROP TABLE IF EXISTS #variables.plain#', {}, { datasource: variables.pg } );
	}

}
