component extends="org.lucee.cfml.test.LuceeTestCase" labels="mssql" {

	function isMsSqlNotSupported() {
		return structCount( server.getDatasource( "mssql" ) ) == 0;
	}

	function run( testResults, testBox ) {

		describe( "LDEV-5970: MSSQL modern mode with parameterized queries", function() {

			it( title="parameterized query with result attribute - modern=false", skip=isMsSqlNotSupported(), body=function() {
				runParameterizedQueryWithResult( modern=false );
			});

			it( title="parameterized query with result attribute - modern=true", skip=isMsSqlNotSupported(), body=function() {
				runParameterizedQueryWithResult( modern=true );
			});

			it( title="simple query with result attribute - modern=false", skip=isMsSqlNotSupported(), body=function() {
				runSimpleQueryWithResult( modern=false );
			});

			it( title="simple query with result attribute - modern=true", skip=isMsSqlNotSupported(), body=function() {
				runSimpleQueryWithResult( modern=true );
			});

			it( title="parameterized query without result - modern=false", skip=isMsSqlNotSupported(), body=function() {
				runParameterizedQueryNoResult( modern=false );
			});

			it( title="parameterized query without result - modern=true", skip=isMsSqlNotSupported(), body=function() {
				runParameterizedQueryNoResult( modern=true );
			});

		});
	}

	private function setMSSQLModern( required boolean value ) {
		var field = createObject( "java", "lucee.runtime.type.QueryImpl" ).getClass().getDeclaredField( "useMSSQLModern" );
		field.setAccessible( true );
		field.setBoolean( nullValue(), arguments.value );
	}

	private function runParameterizedQueryWithResult( required boolean modern ) {
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

	private function runSimpleQueryWithResult( required boolean modern ) {
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

	private function runParameterizedQueryNoResult( required boolean modern ) {
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

}
