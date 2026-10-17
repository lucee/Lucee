component extends="org.lucee.cfml.test.LuceeTestCase" labels="mssql,mysql,postgres" {

	function run( testResults, testBox ) {
		describe( "LDEV-6305 cfqueryparam CF_SQL_FLOAT matches FLOAT columns", function() {

			// MSSQL and Postgres FLOAT columns are double precision, so a double parameter must match
			it( title="mssql: CF_SQL_FLOAT matches a FLOAT column", skip=noDS( "mssql" ), body=function() {
				var ds = server.getDatasource( "mssql" );
				createTable( ds, "CREATE TABLE LDEV6305 ( id INT IDENTITY(1,1) PRIMARY KEY, float_value FLOAT )" );
				try {
					expect( countFloatParam( ds ) ).toBe( 1 );
				}
				finally {
					dropTable( ds );
				}
			});

			it( title="postgres: CF_SQL_FLOAT matches a FLOAT column", skip=noDS( "postgres" ), body=function() {
				var ds = server.getDatasource( "postgres" );
				createTable( ds, "CREATE TABLE LDEV6305 ( id SERIAL PRIMARY KEY, float_value FLOAT )" );
				try {
					expect( countFloatParam( ds ) ).toBe( 1 );
				}
				finally {
					dropTable( ds );
				}
			});

			// MySQL FLOAT is single precision, equality on it is unreliable by design;
			// the parameter must behave exactly like the literal value (no regression from binding as double)
			it( title="mysql: CF_SQL_FLOAT on a single precision FLOAT column behaves like the literal", skip=noDS( "mysql" ), body=function() {
				var ds = server.getDatasource( "mysql" );
				createTable( ds, "CREATE TABLE LDEV6305 ( id INT AUTO_INCREMENT PRIMARY KEY, float_value FLOAT )" );
				try {
					var literal = queryExecute( "SELECT id FROM LDEV6305 WHERE float_value = 2.01", {}, { datasource: ds } ).recordcount;
					expect( countFloatParam( ds ) ).toBe( literal );
				}
				finally {
					dropTable( ds );
				}
			});

			it( title="mysql: CF_SQL_FLOAT matches a DOUBLE column", skip=noDS( "mysql" ), body=function() {
				var ds = server.getDatasource( "mysql" );
				createTable( ds, "CREATE TABLE LDEV6305 ( id INT AUTO_INCREMENT PRIMARY KEY, float_value DOUBLE )" );
				try {
					expect( countFloatParam( ds ) ).toBe( 1 );
				}
				finally {
					dropTable( ds );
				}
			});
		});
	}

	private numeric function countFloatParam( required struct ds ) {
		return queryExecute(
			"SELECT id FROM LDEV6305 WHERE float_value = :val",
			{ val: { value: 2.01, cfsqltype: "CF_SQL_FLOAT" } },
			{ datasource: arguments.ds }
		).recordcount;
	}

	private function createTable( required struct ds, required string createSQL ) {
		dropTable( arguments.ds );
		queryExecute( arguments.createSQL, {}, { datasource: arguments.ds } );
		queryExecute( "INSERT INTO LDEV6305 ( float_value ) VALUES ( 2.01 )", {}, { datasource: arguments.ds } );
	}

	private function dropTable( required struct ds ) {
		queryExecute( "DROP TABLE IF EXISTS LDEV6305", {}, { datasource: arguments.ds } );
	}

	private boolean function noDS( required string db ) {
		return isEmpty( server.getDatasource( arguments.db ) );
	}
}
