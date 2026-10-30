component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, testBox ) {
		describe( "LDEV-3182 QoQ syntax error exception", function() {

			it( title="unexpected token: short single line message, expected tokens in detail", body=function( currentSpec ) {
				var e = catchQoQ( "select name from q where id eq 0" );
				expect( e.type ).toBe( "database" );
				expect( e.message ).toMatch( '^Encountered "eq" at line \d+, column \d+\.$' );
				expect( e.message ).notToInclude( "Was expecting" );
				expect( find( chr( 10 ), e.message ) ).toBe( 0 );
				expect( find( chr( 13 ), e.message ) ).toBe( 0 );
				expect( e.detail ).toInclude( "Was expecting one of:" );
				expect( e.detail ).toInclude( '"AND" ...' );
				expect( e.detail ).toInclude( '"BETWEEN" ...' );
				expect( e.detail ).toInclude( '"ORDER" ...' );
				expect( e.detail ).toInclude( '"=" ...' );
			});

			it( title="duplicate keyword: short single line message, expected tokens in detail", body=function( currentSpec ) {
				var e = catchQoQ( "select name from q where where id eq 0" );
				expect( e.type ).toBe( "database" );
				expect( e.message ).toMatch( '^Encountered "where" at line \d+, column \d+\.$' );
				expect( find( chr( 10 ), e.message ) ).toBe( 0 );
				expect( find( chr( 13 ), e.message ) ).toBe( 0 );
				expect( e.detail ).toInclude( "Was expecting one of:" );
				expect( e.detail ).toInclude( '"COUNT" ...' );
				expect( e.detail ).toInclude( '"EXISTS" ...' );
				expect( e.detail ).toInclude( "<S_IDENTIFIER> ..." );
			});

			it( title="cfquery tag with a multi line statement reports line and column in the message", body=function( currentSpec ) {
				var q = queryNew( "name,id" );
				var e = {};
				try {
					query name="local.q2" dbtype="query" {
						echo( "
    select	name
    from 	q
    where 	id eq 0
" );
					}
				}
				catch ( any ex ) {
					e = ex;
				}
				expect( structIsEmpty( e ) ).toBeFalse( "the query should have failed" );
				expect( e.message ).toMatch( '^Encountered "eq" at line \d+, column \d+\.$' );
				expect( find( chr( 10 ), e.message ) ).toBe( 0 );
				expect( e.detail ).toInclude( "Was expecting one of:" );
				expect( e.detail ).toInclude( '"AND" ...' );
			});

			it( title="the failing sql is still part of the exception", body=function( currentSpec ) {
				var e = catchQoQ( "select name from q where id eq 0" );
				expect( e.sql ).toInclude( "id eq 0" );
			});

		});
	}

	private struct function catchQoQ( required string sql ) {
		var q = queryNew( "name,id" );
		try {
			queryExecute( arguments.sql, {}, { dbtype: "query" } );
		}
		catch ( any e ) {
			return e;
		}
		throw( message="expected a QoQ syntax error for [#arguments.sql#]" );
	}

}
