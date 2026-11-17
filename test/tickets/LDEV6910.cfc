component extends="org.lucee.cfml.test.LuceeTestCase" labels="datasource" {

	function run( testResults, testBox ) {
		describe( "LDEV-6910 {path} and {mode} placeholders of the file based JDBC drivers get a default", function() {

			it( title="H2: {path} gets the default directory and the empty MODE is removed", body=function() {
				var result = fill( "jdbc:h2:{path}mydb;MODE={mode}" );
				expect( result ).toBe( "jdbc:h2:" & defaultPath() & "mydb" );
			});

			it( title="H2: MODE in the middle of the connection string is removed", body=function() {
				var result = fill( "jdbc:h2:{path}mydb;MODE={mode};AUTO_SERVER=TRUE" );
				expect( result ).toBe( "jdbc:h2:" & defaultPath() & "mydb;AUTO_SERVER=TRUE" );
			});

			it( title="HSQLDB: {path} gets the default directory", body=function() {
				expect( fill( "jdbc:hsqldb:file:{path}mydb" ) ).toBe( "jdbc:hsqldb:file:" & defaultPath() & "mydb" );
			});

			it( title="DuckDB: {path} gets the default directory", body=function() {
				expect( fill( "jdbc:duckdb:file:{path}mydb" ) ).toBe( "jdbc:duckdb:file:" & defaultPath() & "mydb" );
			});

			it( title="Derby: an empty {mode} does not leave an empty sub protocol", body=function() {
				expect( fill( "jdbc:derby:{mode}:{path}mydb" ) ).toBe( "jdbc:derby:" & defaultPath() & "mydb" );
			});

			it( title="the default path is an absolute, existing directory", body=function() {
				var path = defaultPath();
				expect( directoryExists( path ) ).toBeTrue();
				expect( right( path, 1 ) ).toBe( "/" );
			});

			it( title="connection strings without placeholders are not changed", body=function() {
				expect( fill( "jdbc:h2:/tmp/mydb;MODE=MySQL" ) ).toBe( "jdbc:h2:/tmp/mydb;MODE=MySQL" );
				expect( fill( "jdbc:postgresql://localhost:5432/test" ) ).toBe( "jdbc:postgresql://localhost:5432/test" );
			});

		});
	}

	private string function fill( required string connStr ) {
		return createObject( "java", "lucee.runtime.db.DataSourceImpl" ).fillFileDatabaseDefaults( getPageContext().getConfig(), arguments.connStr );
	}

	private string function defaultPath() {
		var path = replace( getPageContext().getConfig().getConfigDir().getRealResource( "db" ).getAbsolutePath(), "\", "/", "all" );
		return right( path, 1 ) == "/" ? path : path & "/";
	}

}
