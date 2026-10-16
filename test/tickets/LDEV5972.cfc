component extends="org.lucee.cfml.test.LuceeTestCase" labels="mssql" {

	variables.tableName = "ldev5972_test";

	function isMsSqlNotSupported() {
		return structCount( server.getDatasource( "mssql" ) ) == 0;
	}

	function afterAll() {
		if ( isMsSqlNotSupported() ) return;
		var mssql = server.getDatasource( "mssql" );
		queryExecute( "IF OBJECT_ID('#variables.tableName#', 'U') IS NOT NULL DROP TABLE #variables.tableName#", {}, { datasource: mssql } );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-5972: MSSQL RAISERROR handling", function() {

			describe( "RAISERROR after SELECT", function() {
				it( title="modern=false (RAISERROR silently ignored - known limitation)", skip=isMsSqlNotSupported(), body=function() {
					runRaiserrorAfterSelect( modern=false, expectedMessage="[no exception found]" );
				});
				it( title="modern=true (RAISERROR properly caught)", skip=isMsSqlNotSupported(), body=function() {
					runRaiserrorAfterSelect( modern=true, expectedMessage="Oops! Something went wrong!" );
				});
			});

			describe( "RAISERROR after INSERT", function() {
				it( title="modern=false (RAISERROR silently ignored - known limitation)", skip=isMsSqlNotSupported(), body=function() {
					runRaiserrorAfterInsert( modern=false, expectedMessage="[no exception found]" );
				});
				it( title="modern=true (RAISERROR properly caught)", skip=isMsSqlNotSupported(), body=function() {
					runRaiserrorAfterInsert( modern=true, expectedMessage="Insert failed!" );
				});
			});

			describe( "RAISERROR with multiple statements", function() {
				it( title="modern=false (RAISERROR silently ignored - known limitation)", skip=isMsSqlNotSupported(), body=function() {
					runRaiserrorMultipleStatements( modern=false, expectedMessage="[no exception found]" );
				});
				it( title="modern=true (RAISERROR properly caught)", skip=isMsSqlNotSupported(), body=function() {
					runRaiserrorMultipleStatements( modern=true, expectedMessage="Multi-statement error!" );
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

	private function runRaiserrorAfterSelect( required boolean modern, required string expectedMessage ) {
		setMSSQLModern( arguments.modern );

		var mssql = server.getDatasource( "mssql" );
		var exceptionMessage = "[no exception found]";

		try {
			queryExecute(
				"SELECT 1 as col1; RAISERROR('Oops! Something went wrong!', 16, 1);",
				{},
				{ datasource: mssql }
			);
		} catch ( any e ) {
			exceptionMessage = e.message;
		}

		expect( exceptionMessage ).toBe( arguments.expectedMessage );
	}

	private function runRaiserrorAfterInsert( required boolean modern, required string expectedMessage ) {
		setMSSQLModern( arguments.modern );
		createTestTable();

		var mssql = server.getDatasource( "mssql" );
		var exceptionMessage = "[no exception found]";

		try {
			queryExecute(
				"INSERT INTO #variables.tableName# (name) VALUES ('test'); RAISERROR('Insert failed!', 16, 1);",
				{},
				{ datasource: mssql }
			);
		} catch ( any e ) {
			exceptionMessage = e.message;
		}

		expect( exceptionMessage ).toBe( arguments.expectedMessage );
	}

	private function runRaiserrorMultipleStatements( required boolean modern, required string expectedMessage ) {
		setMSSQLModern( arguments.modern );

		var mssql = server.getDatasource( "mssql" );
		var exceptionMessage = "[no exception found]";

		try {
			queryExecute(
				"
				DECLARE @test TABLE (id INT PRIMARY KEY)
				INSERT INTO @test (id) VALUES (2), (3), (1)
				RAISERROR('Multi-statement error!', 16, 1);
				SELECT id FROM @test ORDER BY id ASC
				",
				{},
				{ datasource: mssql }
			);
		} catch ( any e ) {
			exceptionMessage = e.message;
		}

		expect( exceptionMessage ).toBe( arguments.expectedMessage );
	}

}
