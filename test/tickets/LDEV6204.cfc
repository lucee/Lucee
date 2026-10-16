<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="transaction" {

	function run( testResults, testBox ) {

		describe( "getTransactionIsolation()", function() {

			it( "returns empty string outside a transaction", function() {
				expect( getTransactionIsolation() ).toBe( "" );
			} );

			it( "returns empty string when no explicit isolation set", function() {
				transaction {
					expect( getTransactionIsolation() ).toBe( "" );
				}
			} );

			it( "returns read_uncommitted", function() {
				transaction isolation="read_uncommitted" {
					expect( getTransactionIsolation() ).toBe( "read_uncommitted" );
				}
			} );

			it( "returns read_committed", function() {
				transaction isolation="read_committed" {
					expect( getTransactionIsolation() ).toBe( "read_committed" );
				}
			} );

			it( "returns repeatable_read", function() {
				transaction isolation="repeatable_read" {
					expect( getTransactionIsolation() ).toBe( "repeatable_read" );
				}
			} );

			it( "returns serializable", function() {
				transaction isolation="serializable" {
					expect( getTransactionIsolation() ).toBe( "serializable" );
				}
			} );

			it( "resets after transaction ends", function() {
				transaction isolation="serializable" {
					expect( getTransactionIsolation() ).toBe( "serializable" );
				}
				expect( getTransactionIsolation() ).toBe( "" );
			} );

		} );

		describe( "getTransactionIsolation( datasource )", function() {

			it( "returns datasource default when no explicit isolation set", function() {
				var ds = server.getDatasource( "h2", server._getTempDir( "LDEV6204" ) );
				application action="update" datasources={ "LDEV6204": ds };
				var result = getTransactionIsolation( "LDEV6204" );
				// just verify we get a non-empty string back (db default varies by driver)
				expect( len( result ) ).toBeGT( 0 );
			} );

			it( "returns explicit isolation even when datasource provided", function() {
				var ds = server.getDatasource( "h2", server._getTempDir( "LDEV6204" ) );
				application action="update" datasources={ "LDEV6204": ds };
				transaction isolation="serializable" {
					expect( getTransactionIsolation( "LDEV6204" ) ).toBe( "serializable" );
				}
			} );

		} );

	}

}
</cfscript>
