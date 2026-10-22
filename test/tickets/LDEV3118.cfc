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

			// LDEV-3118 tinyint is unsigned (0..255) like in ACF, a negative value must fail
			// instead of being wrapped by the driver (-128 became 128)
			it( title="negative tinyint values throw on every datasource", skip=notHasH2(), body=function() {
				expectInvalid( "tinyint", -1 );
				expectInvalid( "tinyint", "-1" );
				expectInvalid( "tinyint", -128 );
				expect( expectExceptionMessage( "tinyint", -128 ) )
					.toBe( "Invalid data [-128] for CFSQLTYPE CF_SQL_TINYINT" );
			});

			it( title="values at the range limits still work", skip=notHasH2(), body=function() {
				expect( param( "integer", 2147483647 ) ).toBe( 2147483647 );
				expect( param( "integer", "-2147483648" ) ).toBe( -2147483648 );
				expect( param( "smallint", 32767 ) ).toBe( 32767 );
				expect( param( "tinyint", 0 ) ).toBe( 0 );
				expect( param( "tinyint", 127 ) ).toBe( 127 );
				expect( param( "bigint", "9223372036854775807" ) ).toBe( "9223372036854775807" );
				expect( param( "integer", 123 ) ).toBe( 123 );
			});

			// MSSQL tinyint is unsigned 0..255, Lucee must not reject it (H2 itself is signed, so the driver may still refuse it)
			it( title="unsigned tinyint values up to 255 pass the Lucee range check", skip=notHasH2(), body=function() {
				expectNotRejectedByLucee( "tinyint", 200 );
				expectNotRejectedByLucee( "tinyint", "255" );
			});

			it( title="tinyint values from 0 to 255 still work on MSSQL", skip=notHasMSSQL(), body=function() {
				var mssql = server.getDatasource( "mssql" );
				expect( param( "tinyint", 0, mssql ) ).toBe( 0 );
				expect( param( "tinyint", 127, mssql ) ).toBe( 127 );
				expect( param( "tinyint", "255", mssql ) ).toBe( 255 );
				expectInvalid( "tinyint", 256, mssql );
				expectInvalid( "tinyint", -1, mssql );
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

	// LDEV-3118 ACF compatible message, compared exactly like in the previous test case (_LDEV3118.cfc)
	private function expectInvalid( required string type, required any value, any datasource=variables.ds ) {
		var expected = "Invalid data [#arguments.value#] for CFSQLTYPE CF_SQL_#ucase( arguments.type )#";
		expect( expectExceptionMessage( arguments.type, arguments.value, arguments.datasource ) ).toBe( expected );
	}

	private function expectExceptionMessage( required string type, required any value, any datasource=variables.ds ) {
		try {
			param( arguments.type, arguments.value, arguments.datasource );
		}
		catch ( any e ) {
			return e.message;
		}
		return "";
	}

	private boolean function notHasH2() {
		return structIsEmpty( server.getDatasource( "h2", server._getTempDir( "LDEV3118" ) ) );
	}

	private boolean function notHasMSSQL() {
		return structIsEmpty( server.getDatasource( "mssql" ) );
	}
}
