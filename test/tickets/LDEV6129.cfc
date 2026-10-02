component extends="org.lucee.cfml.test.LuceeTestCase" labels="datasource,h2" {

	function beforeAll() {
		variables.dbDir = server._getTempDir( "LDEV6129" );
		variables.dsName = "LDEV6129_h2";
	}

	function afterAll() {
		if ( directoryExists( variables.dbDir ) ) directoryDelete( variables.dbDir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6129 - managed transaction connection must not leak when it breaks inside the transaction", function() {

			it( title="connection is not leaked when resetting autoCommit at transaction end fails", skip=isH2NotSupported(), body=function( currentSpec ) {
				var ds = server.getDatasource( "h2", variables.dbDir );
				ds.connectionLimit = 1;
				ds.maxTotal = 1;
				application action="update" datasources={ "#variables.dsName#": ds };

				query datasource=variables.dsName { echo( "SELECT 1" ); }
				expect( getPoolActive( variables.dsName ) ).toBe( 0 );

				try {
					transaction {
						query datasource=variables.dsName { echo( "SELECT 1" ); }
						// closes the database and with it the connection held by the transaction,
						// so setAutoCommit(true) in DatasourceManagerImpl.end() throws
						query datasource=variables.dsName { echo( "SHUTDOWN" ); }
					}
				}
				catch ( any e ) {
					// expected, the connection is closed
				}

				expect( getPoolActive( variables.dsName ) ).toBe( 0, "connection leaked after transaction end failed" );

				query name="local.q" datasource=variables.dsName { echo( "SELECT 1 AS ok" ); }
				expect( q.ok ).toBe( 1 );
				expect( getPoolActive( variables.dsName ) ).toBe( 0 );
			});

		});
	}

	private numeric function getPoolActive( required string dsName ) {
		var metrics = getSystemMetrics();
		if ( !structKeyExists( metrics, "datasourceConnections" ) ) return 0;
		var total = 0;
		loop collection=metrics.datasourceConnections item="local.pool" {
			if ( ( pool.name ?: "" ) == arguments.dsName ) total += pool.activeDatasourceConnections ?: 0;
		}
		return total;
	}

	private boolean function isH2NotSupported() {
		return structIsEmpty( server.getDatasource( "h2", server._getTempDir( "LDEV6129" ) ) );
	}

}
