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
				expectInvalid( "tinyint", 260 );
				expectInvalid( "bigint", "9300000000000000000" );
				expectInvalid( "bigint", "-9300000000000000000" );
			});

			it( title="values at the range limits still work", skip=notHasH2(), body=function() {
				expect( param( "integer", 2147483647 ) ).toBe( 2147483647 );
				expect( param( "integer", "-2147483648" ) ).toBe( -2147483648 );
				expect( param( "smallint", 32767 ) ).toBe( 32767 );
				expect( param( "tinyint", 127 ) ).toBe( 127 );
				expect( param( "bigint", "9223372036854775807" ) ).toBe( "9223372036854775807" );
				expect( param( "integer", 123 ) ).toBe( 123 );
			});

		});
	}

	private function param( required string type, required any value ) {
		query name="local.q" datasource=variables.ds {
			echo( "select " );
			queryparam cfsqltype="cf_sql_#arguments.type#" value=arguments.value;
			echo( " as r" );
		}
		return q.r;
	}

	private function expectInvalid( required string type, required any value ) {
		var t = arguments.type;
		var v = arguments.value;
		expect( function() {
			param( t, v );
		} ).toThrow( message="Invalid data*" );
	}

	private boolean function notHasH2() {
		return structIsEmpty( server.getDatasource( "h2", server._getTempDir( "LDEV3118" ) ) );
	}
}
