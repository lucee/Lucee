component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults, testBox ) {
		describe( "LDEV-6202 QueryExecute passes entire options struct as tag attributes, leaking datasource keys", function() {

			it( title="queryExecute ignores datasource definition keys passed in options", body=function( currentSpec ) {
				var src = getSource();
				var result = queryExecute( "select * from src", {}, {
					dbtype: "query",
					host: "localhost",
					port: "1433",
					database: "test",
					tablespace: "test",
					class: "com.microsoft.sqlserver.jdbc.SQLServerDriver",
					connectionString: "jdbc:sqlserver://localhost:1433;DATABASENAME=test"
				} );
				expect( result.recordCount ).toBe( 3 );
			});

			it( title="queryExecute does not apply options that are not cfquery attributes (returnVariable)", body=function( currentSpec ) {
				var src = getSource();
				var result = queryExecute( "select * from src", {}, { dbtype: "query", returnVariable: false } );
				expect( isNull( result ) ).toBeFalse();
				expect( result.recordCount ).toBe( 3 );
			});

			it( title="queryExecute still applies the cfquery attributes result and maxRows from options", body=function( currentSpec ) {
				var src = getSource();
				var result = queryExecute( "select * from src", {}, { dbtype: "query", result: "local.queryResult", maxRows: 2 } );
				expect( result.recordCount ).toBe( 2 );
				expect( local.queryResult ).toHaveKey( "recordcount" );
				expect( local.queryResult.recordcount ).toBe( 2 );
			});

			it( title="queryExecute still applies the cfquery attributes returnType and columnKey from options", body=function( currentSpec ) {
				var src = getSource();
				var result = queryExecute( "select * from src", {}, { dbtype: "query", returnType: "struct", columnKey: "id" } );
				expect( result ).toBeStruct();
				expect( result ).toHaveKey( "2" );
				expect( result[ "2" ].name ).toBe( "b" );
			});

			it( title="queryExecute still applies cfquery attribute aliases from options (column for columnKey)", body=function( currentSpec ) {
				var src = getSource();
				var result = queryExecute( "select * from src", {}, { dbtype: "query", returnType: "struct", column: "id" } );
				expect( result ).toBeStruct();
				expect( result ).toHaveKey( "3" );
			});

		});
	}

	private query function getSource() {
		return queryNew( "id,name", "integer,varchar", [ [ 1, "a" ], [ 2, "b" ], [ 3, "c" ] ] );
	}

}
