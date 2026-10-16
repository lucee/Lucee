component extends="org.lucee.cfml.test.LuceeTestCase" labels="mssql" {

	variables.tableName = "ldev5970_test";

	function isMsSqlNotSupported() {
		return structCount( server.getDatasource( "mssql" ) ) == 0;
	}

	function afterAll() {
		if ( isMsSqlNotSupported() ) return;
		var mssql = server.getDatasource( "mssql" );
		queryExecute( "IF OBJECT_ID('#variables.tableName#', 'U') IS NOT NULL DROP TABLE #variables.tableName#", {}, { datasource: mssql } );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-5970: MSSQL modern mode", function() {

			describe( "SELECT parameterized with result attribute", function() {
				it( title="modern=false", skip=isMsSqlNotSupported(), body=function() {
					runParameterizedSelectWithResult( modern=false );
				});
				it( title="modern=true", skip=isMsSqlNotSupported(), body=function() {
					runParameterizedSelectWithResult( modern=true );
				});
			});

			describe( "SELECT simple with result attribute", function() {
				it( title="modern=false", skip=isMsSqlNotSupported(), body=function() {
					runSimpleSelectWithResult( modern=false );
				});
				it( title="modern=true", skip=isMsSqlNotSupported(), body=function() {
					runSimpleSelectWithResult( modern=true );
				});
			});

			describe( "SELECT parameterized without result attribute", function() {
				it( title="modern=false", skip=isMsSqlNotSupported(), body=function() {
					runParameterizedSelectNoResult( modern=false );
				});
				it( title="modern=true", skip=isMsSqlNotSupported(), body=function() {
					runParameterizedSelectNoResult( modern=true );
				});
			});

			describe( "INSERT with identity key", function() {
				it( title="modern=false", skip=isMsSqlNotSupported(), body=function() {
					runInsertWithGeneratedKey( modern=false );
				});
				it( title="modern=true", skip=isMsSqlNotSupported(), body=function() {
					runInsertWithGeneratedKey( modern=true );
				});
			});

			describe( "INSERT parameterized with identity key", function() {
				it( title="modern=false", skip=isMsSqlNotSupported(), body=function() {
					runParameterizedInsertWithGeneratedKey( modern=false );
				});
				it( title="modern=true", skip=isMsSqlNotSupported(), body=function() {
					runParameterizedInsertWithGeneratedKey( modern=true );
				});
			});

		});
	}

	private function setMSSQLModern( required boolean value ) {
		var field = createObject( "java", "lucee.runtime.type.QueryImpl" ).getClass().getDeclaredField( "useMSSQLModern" );
		field.setAccessible( true );
		field.setBoolean( nullValue(), arguments.value );
	}

	private function createTestTable() {
		var mssql = server.getDatasource( "mssql" );
		queryExecute( "IF OBJECT_ID('#variables.tableName#', 'U') IS NOT NULL DROP TABLE #variables.tableName#", {}, { datasource: mssql } );
		queryExecute( "CREATE TABLE #variables.tableName# ( id INT IDENTITY(1,1) PRIMARY KEY, name VARCHAR(100) )", {}, { datasource: mssql } );
	}

	private function runParameterizedSelectWithResult( required boolean modern ) {
		setMSSQLModern( arguments.modern );

		var mssql = server.getDatasource( "mssql" );
		var result = queryExecute(
			"SELECT TOP 10 name FROM sys.objects WHERE object_id > :objectId",
			{ objectId: { value: 1, cfsqltype: "CF_SQL_INTEGER" } },
			{ datasource: mssql, result: "local.queryResult" }
		);

		expect( result ).toBeQuery();
		expect( result.recordCount ).toBeGTE( 0 );
		expect( local.queryResult ).toBeStruct();
		expect( local.queryResult ).toHaveKey( "recordcount" );
	}

	private function runSimpleSelectWithResult( required boolean modern ) {
		setMSSQLModern( arguments.modern );

		var mssql = server.getDatasource( "mssql" );
		var result = queryExecute(
			"SELECT TOP 5 name FROM sys.objects",
			{},
			{ datasource: mssql, result: "local.queryResult" }
		);

		expect( result ).toBeQuery();
		expect( result.recordCount ).toBeGTE( 0 );
		expect( local.queryResult ).toBeStruct();
	}

	private function runParameterizedSelectNoResult( required boolean modern ) {
		setMSSQLModern( arguments.modern );

		var mssql = server.getDatasource( "mssql" );
		var result = queryExecute(
			"SELECT TOP 10 name FROM sys.objects WHERE object_id > :objectId",
			{ objectId: { value: 1, cfsqltype: "CF_SQL_INTEGER" } },
			{ datasource: mssql }
		);

		expect( result ).toBeQuery();
		expect( result.recordCount ).toBeGTE( 0 );
	}

	private function runInsertWithGeneratedKey( required boolean modern ) {
		setMSSQLModern( arguments.modern );
		createTestTable();

		var mssql = server.getDatasource( "mssql" );
		var result = queryExecute(
			"INSERT INTO #variables.tableName# (name) VALUES ('test')",
			{},
			{ datasource: mssql, result: "local.queryResult" }
		);

		expect( local.queryResult ).toBeStruct();
		expect( local.queryResult ).toHaveKey( "generatedKey" );
		expect( local.queryResult.generatedKey ).toBeNumeric();
		expect( local.queryResult.generatedKey ).toBeGTE( 1 );
	}

	private function runParameterizedInsertWithGeneratedKey( required boolean modern ) {
		setMSSQLModern( arguments.modern );
		createTestTable();

		var mssql = server.getDatasource( "mssql" );
		var result = queryExecute(
			"INSERT INTO #variables.tableName# (name) VALUES (:name)",
			{ name: { value: "test param", cfsqltype: "CF_SQL_VARCHAR" } },
			{ datasource: mssql, result: "local.queryResult" }
		);

		expect( local.queryResult ).toBeStruct();
		expect( local.queryResult ).toHaveKey( "generatedKey" );
		expect( local.queryResult.generatedKey ).toBeNumeric();
		expect( local.queryResult.generatedKey ).toBeGTE( 1 );
	}

}
