component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function beforeAll() {
		variables.ds = server.getDatasource( "h2", server._getTempDir( "LDEV3118" ) );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3118 cfqueryparam integer types must not silently overflow", function() {

			it( title="values out of range throw", skip=notHasH2(), body=function() {
				expectInvalid( "integer", 2200000000 );
				expectInvalid( "integer", "2200000000" );
				expectInvalid( "integer", -2147483649 );
				expectInvalid( "smallint", 32768 );
				expectInvalid( "smallint", "-32769" );
				expectInvalid( "tinyint", 256 );
				expectInvalid( "tinyint", "256" );
				expectInvalid( "tinyint", -129 );
				expectInvalid( "bigint", "9300000000000000000" );
				expectInvalid( "bigint", "-9300000000000000000" );
			});

			it( title="values at the range limits still work", skip=notHasH2(), body=function() {
				expect( param( "integer", 2147483647 ) ).toBe( 2147483647 );
				expect( param( "integer", "-2147483648" ) ).toBe( -2147483648 );
				expect( param( "smallint", 32767 ) ).toBe( 32767 );
				expect( param( "tinyint", 127 ) ).toBe( 127 );
				expect( param( "tinyint", -128 ) ).toBe( -128 );
				expect( param( "bigint", "9223372036854775807" ) ).toBe( "9223372036854775807" );
				expect( param( "integer", 123 ) ).toBe( 123 );
			});

			// MSSQL tinyint is unsigned 0..255, Lucee must not reject it (H2 itself is signed, so the driver may still refuse it)
			it( title="unsigned tinyint values up to 255 pass the Lucee range check", skip=notHasH2(), body=function() {
				expectNotRejectedByLucee( "tinyint", 200 );
				expectNotRejectedByLucee( "tinyint", "255" );
			});

			it( title="negative tinyint values still work on a signed tinyint", skip=notHasH2(), body=function() {
				expect( param( "tinyint", -1 ) ).toBe( -1 );
				expect( param( "tinyint", "-1" ) ).toBe( -1 );
			});

			it( title="tinyint values from 0 to 255 work on MSSQL", skip=notHasMSSQL(), body=function() {
				var mssql = server.getDatasource( "mssql" );
				expect( param( "tinyint", 0, mssql ) ).toBe( 0 );
				expect( param( "tinyint", 127, mssql ) ).toBe( 127 );
				expect( param( "tinyint", "255", mssql ) ).toBe( 255 );
				expectInvalid( "tinyint", 256, mssql );
			});

			// MSSQL tinyint is unsigned, the driver wraps a negative value (-128 became 128)
			it( title="negative tinyint values throw on MSSQL", skip=notHasMSSQL(), body=function() {
				var mssql = server.getDatasource( "mssql" );
				expectInvalid( "tinyint", -1, mssql );
				expectInvalid( "tinyint", "-1", mssql );
				expectInvalid( "tinyint", -128, mssql );
			});

			it( title="negative tinyint values still work on MySQL", skip=notHasMySQL(), body=function() {
				var mysql = server.getDatasource( "mysql" );
				expect( param( "tinyint", -1, mysql ) ).toBe( -1 );
				expect( param( "tinyint", -128, mysql ) ).toBe( -128 );
				expect( param( "tinyint", 127, mysql ) ).toBe( 127 );
				expectInvalid( "tinyint", 256, mysql );
			});

		});
	}

	private function expectNotRejectedByLucee( required string type, required any value ) {
		try {
			param( arguments.type, arguments.value );
		}
		catch ( any e ) {
			expect( e.message ).notToInclude( "Invalid data [" );
		}
	}

	private function param( required string type, required any value, any datasource=variables.ds ) {
		query name="local.q" datasource=arguments.datasource {
			echo( "select " );
			queryparam cfsqltype="cf_sql_#arguments.type#" value=arguments.value;
			echo( " as r" );
		}
		return q.r;
	}

	private function expectInvalid( required string type, required any value, any datasource=variables.ds ) {
		var t = arguments.type;
		var v = arguments.value;
		var d = arguments.datasource;
		expect( function() {
			param( t, v, d );
		} ).toThrow( message="Invalid data*" );
	}

	private boolean function notHasH2() {
		return structIsEmpty( server.getDatasource( "h2", server._getTempDir( "LDEV3118" ) ) );
	}

	private boolean function notHasMSSQL() {
		return structIsEmpty( server.getDatasource( "mssql" ) );
	}

	private boolean function notHasMySQL() {
		return structIsEmpty( server.getDatasource( "mysql" ) );
	}
}
