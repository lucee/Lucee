component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {

	private numeric function getIdleConnections( required string dsName ) {
		var info = getSystemMetrics();
		for ( var key in info.datasourceConnections ) {
			if ( info.datasourceConnections[ key ].name == arguments.dsName ) {
				return info.datasourceConnections[ key ].idleDatasourceConnections;
			}
		}
		return 0;
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5963 - Pool eviction", function() {
			it( "clears idle connections with DBPoolClear force=true", function() {
				// Get MySQL datasource credentials
				var creds = server.getDatasource( "mysql" );
				var dsName = "LDEV5963_ds";

				// Create datasource
				var dsConfig = {
					class: creds.class,
					bundleName: creds.bundleName,
					bundleVersion: creds.bundleVersion,
					connectionString: creds.connectionString,
					username: creds.username,
					password: creds.password
				};

				application action="update" datasources={ "#dsName#": dsConfig };

				try {
					// Use a connection to create it in the pool
					queryExecute( "SELECT 1", {}, { datasource: dsName } );

					// Connection should be idle now
					expect( getIdleConnections( dsName ) ).toBeGTE( 1, "Should have at least 1 idle connection" );

					// Force clear all idle connections
					DBPoolClear( dsName, true );

					// Verify pool is empty
					expect( getIdleConnections( dsName ) ).toBe( 0, "Should have 0 idle connections after clear" );
				}
				finally {
					// Cleanup - clear pool and remove datasource
					DBPoolClear( dsName );
					application action="update" datasources={};
				}
			});
		});
	}

}
