component extends="org.lucee.cfml.test.LuceeTestCase" labels="datasource,osgi" {

	function beforeAll() {
		variables.dsName = "LDEV6722_ds";
		variables.ds = server.getDatasource( "h2", server._getTempDir( "LDEV6722" ) );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6722: a datasource must not keep using a JDBC driver from an uninstalled bundle", function() {

			it( title="new connections re-resolve the driver after its bundle got uninstalled", skip=notHasH2(), body=function() {
				application action="update" datasources={ "#variables.dsName#": variables.ds };
				var cd = "";
				try {
					expect( queryExecute( "SELECT 1 AS one", {}, { datasource: variables.dsName } ).one ).toBe( 1 );

					var pool = getPool( variables.dsName );
					cd = pool.getFactory().getDatasource().getClassDefinition();
					var bundle = getBundle( getConnectionClass( pool ) );
					expect( isNull( bundle ) ).toBeFalse( "the JDBC driver should be loaded from an OSGi bundle" );

					// simulates the extension cleanup, which uninstalls the driver bundle of a removed extension
					bundle.uninstall();
					expect( isUninstalled( bundle ) ).toBeTrue();

					// drop the pooled connections, so the next query has to create a new connection with the (cached) driver
					DBPoolClear( variables.dsName );

					expect( queryExecute( "SELECT 1 AS one", {}, { datasource: variables.dsName } ).one ).toBe( 1 );

					var newBundle = getBundle( getConnectionClass( getPool( variables.dsName ) ) );
					expect( isNull( newBundle ) ).toBeFalse();
					expect( isUninstalled( newBundle ) ).toBeFalse( "new connections must not be created by a driver from the uninstalled bundle" );
				}
				finally {
					DBPoolClear( variables.dsName );
					application action="update" datasources={};
					// make sure the driver bundle is available again for other tests
					if ( isObject( cd ) ) cd.getClazz( true );
				}
			});

		});
	}

	private function getPool( required string dsName ) {
		var iter = getPageContext().getConfig().getDatasourceConnectionPools().iterator();
		while ( iter.hasNext() ) {
			var p = iter.next();
			if ( p.getFactory().getDatasource().getName() == arguments.dsName ) return p;
		}
		throw( message="no connection pool found for datasource [#arguments.dsName#]" );
	}

	private function getConnectionClass( required pool ) {
		var dc = arguments.pool.borrowObject();
		try {
			return dc.getConnection().getClass();
		}
		finally {
			arguments.pool.returnObject( dc );
		}
	}

	private function getBundle( required clazz ) {
		return createObject( "java", "org.osgi.framework.FrameworkUtil" ).getBundle( arguments.clazz );
	}

	private boolean function isUninstalled( required bundle ) {
		return arguments.bundle.getState() == createObject( "java", "org.osgi.framework.Bundle" ).UNINSTALLED;
	}

	private boolean function notHasH2() {
		return structIsEmpty( server.getDatasource( "h2", server._getTempDir( "LDEV6722" ) ) );
	}

}
