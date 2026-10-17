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
			it( title="evicts idle connections after idleTimeout using DBPoolClear force=false", skip=isMySqlNotSupported(), body=function() {
				// Get MySQL datasource credentials
				var creds = server.getDatasource( "mysql" );
				var dsName = "LDEV5963_ds";

				// Create datasource with 5 minute idleTimeout
				var dsConfig = {
					class: creds.class,
					bundleName: creds.bundleName,
					bundleVersion: creds.bundleVersion,
					connectionString: creds.connectionString,
					username: creds.username,
					password: creds.password,
					idleTimeout: 5 // 5 minutes - should be wired to minEvictableIdleTimeMillis
				};

				application action="update" datasources={ "#dsName#": dsConfig };

				try {
					// Use a connection to create it in the pool
					queryExecute( "SELECT 1", {}, { datasource: dsName } );

					// Connection should be idle now
					expect( getIdleConnections( dsName ) ).toBeGTE( 1, "Should have at least 1 idle connection" );

					// Shortcut the 5-minute idleTimeout via the pool's public setter
					// so the test runs in milliseconds rather than minutes.
					shortenMinIdleEvictable( dsName, 100 );
					sleep( 200 ); // > minEvictableIdleTimeMillis

					// Evict expired idle connections (force=false uses pool.evict())
					DBPoolClear( dsName, false );

					// Verify connection was evicted because it exceeded idleTimeout
					expect( getIdleConnections( dsName ) ).toBe( 0, "Should have 0 idle connections after eviction - idleTimeout should be respected" );
				}
				finally {
					// Cleanup - clear pool and remove datasource
					DBPoolClear( dsName );
					application action="update" datasources={};
				}
			});
		});
	}

	private boolean function isMySqlNotSupported() {
		return structIsEmpty( server.getDatasource( service="mysql" ) );
	}

	private void function shortenMinIdleEvictable( required string dsName, required numeric millis ) {
		var iter = getPageContext().getConfig().getDatasourceConnectionPools().iterator();
		while ( iter.hasNext() ) {
			var p = iter.next();
			if ( p.getFactory().getDatasource().getName() == arguments.dsName ) {
				p.setMinEvictableIdleTimeMillis( javaCast( "long", arguments.millis ) );
			}
		}
	}

}
