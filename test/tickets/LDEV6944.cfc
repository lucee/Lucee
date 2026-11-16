component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6944 JDBCDriver fallback templates", function() {

			it( title="mssql fallback includes databaseName={database}", body=function() {
				var dsn = fallbackConnStr( "mssql" );
				expect( dsn ).toBe( "jdbc:sqlserver://{host}:{port};databaseName={database}" );
			});

			it( title="oracle fallback uses thin and does not leave {drivertype} unsubstituted", body=function() {
				var dsn = fallbackConnStr( "oracle" );
				expect( dsn ).toBe( "jdbc:oracle:thin:@{host}:{port}:{database}" );
				expect( dsn ).notToInclude( "{drivertype}" );
			});

			it( title="an explicit connection string still wins over the fallback", body=function() {
				var JDBCDriver = createObject( "java", "lucee.runtime.db.JDBCDriver" );
				var driver = JDBCDriver.init( "Microsoft SQL Server", "mssql", "jdbc:custom://example", javacast( "null", "" ) );
				expect( driver.connStr ).toBe( "jdbc:custom://example" );
			});

			it( title="mysql fallback still places the database in the URL", body=function() {
				expect( fallbackConnStr( "mysql" ) ).toBe( "jdbc:mysql://{host}:{port}/{database}" );
			});

		});
	}

	private string function fallbackConnStr( required string id ) {
		var JDBCDriver = createObject( "java", "lucee.runtime.db.JDBCDriver" );
		// empty connStr triggers JDBCDriver.getById()
		var driver = JDBCDriver.init( arguments.id, arguments.id, "", javacast( "null", "" ) );
		return driver.connStr;
	}

}
