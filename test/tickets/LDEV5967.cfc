component extends="org.lucee.cfml.test.LuceeTestCase" labels="datasource,pool" {

	function beforeAll() {
		variables.adminPassword = request.ServerAdminPassword;

		if ( isH2NotSupported() ) return;

		variables.case1Prefix  = "LDEV5967_c1victim_";
		variables.case1Trigger = "LDEV5967_c1trigger";
		variables.case2Prefix  = "LDEV5967_c2victim_";
		variables.case3Ds      = "LDEV5967_c3";

		variables.victimsCase1 = 20;
		variables.victimsCase2 = 10;

		for ( var i = 1; i <= variables.victimsCase1; i++ ) {
			registerServerDatasource( variables.case1Prefix & i, "jdbc:h2:mem:#variables.case1Prefix##i#" );
		}
		registerServerDatasource( variables.case1Trigger, "jdbc:h2:mem:#variables.case1Trigger#" );

		for ( var i = 1; i <= variables.victimsCase2; i++ ) {
			registerServerDatasource( variables.case2Prefix & i, "jdbc:h2:mem:#variables.case2Prefix##i#" );
		}

		registerServerDatasource( variables.case3Ds, "jdbc:h2:mem:#variables.case3Ds#" );
	}

	function afterAll() {
		if ( isH2NotSupported() ) return;
		for ( var i = 1; i <= variables.victimsCase1; i++ ) {
			removeServerDatasourceIfExists( variables.case1Prefix & i );
		}
		removeServerDatasourceIfExists( variables.case1Trigger );
		for ( var i = 1; i <= variables.victimsCase2; i++ ) {
			removeServerDatasourceIfExists( variables.case2Prefix & i );
		}
		removeServerDatasourceIfExists( variables.case3Ds );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-5967 Datasource Pool Lifecycle Management", function() {

			it( title="request-path sweep no longer evicts quiet pools", skip=isH2NotSupported(), body=function() {
				for ( var i = 1; i <= variables.victimsCase1; i++ ) {
					queryExecute( "select 1", {}, { datasource: variables.case1Prefix & i } );
				}
				dbPoolClear(); // drop idle connections so (numActive+numIdle+numWaiters)==0
				agePoolsByPrefix( variables.case1Prefix, 65000 ); // satisfy lastBorrowed + POOL_MAX_IDLE < now

				// track per-ObjectName so background sweeps of other tests' pools can't skew the count
				var jmxAfterWarm = collectJmxNamesByPrefix( variables.case1Prefix );
				expect( arrayLen( jmxAfterWarm ) ).toBe( variables.victimsCase1 );
				expect( poolCountByPrefix( variables.case1Prefix ) ).toBe( variables.victimsCase1 );

				queryExecute( "select 1", {}, { datasource: variables.case1Trigger } );

				expect( poolCountByPrefix( variables.case1Prefix ) ).toBe( variables.victimsCase1,
					"victims should still be in the map after trigger (no request-path sweep)" );

				var barrier = createObject( "java", "java.util.concurrent.CountDownLatch" ).init( javaCast( "int", 1 ) );
				for ( var i = 1; i <= variables.victimsCase1; i++ ) {
					thread name="ldev5967_c1_#i#" idx=i b=barrier prefix=variables.case1Prefix {
						attributes.b.await();
						queryExecute( "select 1", {}, { datasource: attributes.prefix & attributes.idx } );
					}
				}
				sleep( 200 );
				barrier.countDown();
				for ( var i = 1; i <= variables.victimsCase1; i++ ) {
					thread action="join" name="ldev5967_c1_#i#";
				}

				expect( poolCountByPrefix( variables.case1Prefix ) ).toBe( variables.victimsCase1 );
				expect( arrayLen( collectJmxNamesByPrefix( variables.case1Prefix ) ) ).toBe( variables.victimsCase1 );
				expect( countStillRegistered( jmxAfterWarm ) ).toBe( variables.victimsCase1,
					"every warmed victim MBean still registered (no orphan churn)" );
			});

			it( title="bg-thread sweep destroys quiet pools via pool.close()", skip=isH2NotSupported(), body=function() {
				for ( var i = 1; i <= variables.victimsCase2; i++ ) {
					queryExecute( "select 1", {}, { datasource: variables.case2Prefix & i } );
				}
				dbPoolClear();
				agePoolsByPrefix( variables.case2Prefix, 65000 );

				// capture before sweep — global pool2 MBean count is shared with other pool2 users
				var case2JmxNames = collectJmxNamesByPrefix( variables.case2Prefix );
				expect( poolCountByPrefix( variables.case2Prefix ) ).toBe( variables.victimsCase2 );
				expect( arrayLen( case2JmxNames ) ).toBe( variables.victimsCase2 );

				// first call marks, second call closes — mirrors the two-tick cycle the Controler runs
				getPageContext().getConfig().cleanDatasourceConnectionPools();
				getPageContext().getConfig().cleanDatasourceConnectionPools();

				expect( poolCountByPrefix( variables.case2Prefix ) ).toBe( 0 );
				expect( countStillRegistered( case2JmxNames ) ).toBe( 0, "MBeans unregistered by close()" );

				queryExecute( "select 1", {}, { datasource: variables.case2Prefix & 1 } );
				expect( poolCountByPrefix( variables.case2Prefix ) ).toBe( 1,
					"borrow after sweep recreates the pool" );
			});

			it( title="admin datasource removal tears down the pool", skip=isH2NotSupported(), body=function() {
				queryExecute( "select 1", {}, { datasource: variables.case3Ds } );

				var case3JmxNames = collectJmxNamesByPrefix( variables.case3Ds );
				expect( poolCountForName( variables.case3Ds ) ).toBe( 1 );
				expect( arrayLen( case3JmxNames ) ).toBe( 1 );

				removeServerDatasource( variables.case3Ds );

				expect( poolCountForName( variables.case3Ds ) ).toBe( 0 );
				expect( countStillRegistered( case3JmxNames ) ).toBe( 0 );
			});

		});
	}

	// ---- helpers ----

	private numeric function poolCountByPrefix( required string prefix ) {
		var iter = getPageContext().getConfig().getDatasourceConnectionPools().iterator();
		var n = 0;
		while ( iter.hasNext() ) {
			var p = iter.next();
			if ( left( p.getFactory().getDatasource().getName(), len( arguments.prefix ) ) == arguments.prefix ) n++;
		}
		return n;
	}

	private numeric function poolCountForName( required string dsName ) {
		var iter = getPageContext().getConfig().getDatasourceConnectionPools().iterator();
		var n = 0;
		while ( iter.hasNext() ) {
			var p = iter.next();
			if ( p.getFactory().getDatasource().getName() == arguments.dsName ) n++;
		}
		return n;
	}

	private array function collectJmxNamesByPrefix( required string prefix ) {
		var names = [];
		var iter = getPageContext().getConfig().getDatasourceConnectionPools().iterator();
		while ( iter.hasNext() ) {
			var p = iter.next();
			if ( left( p.getFactory().getDatasource().getName(), len( arguments.prefix ) ) == arguments.prefix ) {
				var jmxName = p.getJmxName();
				if ( !isNull( jmxName ) ) arrayAppend( names, jmxName );
			}
		}
		return names;
	}

	private numeric function countStillRegistered( required array objectNames ) {
		var mbs = createObject( "java", "java.lang.management.ManagementFactory" ).getPlatformMBeanServer();
		var n = 0;
		for ( var on in arguments.objectNames ) {
			if ( mbs.isRegistered( on ) ) n++;
		}
		return n;
	}

	// shortcut lastBorrowed so the eviction predicate is satisfied without sleeping POOL_MAX_IDLE
	private void function agePoolsByPrefix( required string prefix, required numeric ageMillis ) {
		var iter = getPageContext().getConfig().getDatasourceConnectionPools().iterator();
		while ( iter.hasNext() ) {
			var p = iter.next();
			if ( left( p.getFactory().getDatasource().getName(), len( arguments.prefix ) ) == arguments.prefix ) {
				p.setLastBorrowed( javaCast( "long", getTickCount() - arguments.ageMillis ) );
			}
		}
	}

	private void function registerServerDatasource( required string name, required string connectionString ) {
		admin
			action="updateDatasource"
			type="server"
			password="#variables.adminPassword#"
			name="#arguments.name#"
			newName="#arguments.name#"
			classname="org.h2.Driver"
			bundlename="org.lucee.h2"
			dsn="#arguments.connectionString#"
			dbusername="sa"
			dbpassword=""
			connectionLimit="10"
			connectionTimeout="0"
			blob="false"
			clob="false"
			validate="false"
			storage="false"
			allowed_select="true"
			allowed_insert="true"
			allowed_update="true"
			allowed_delete="true"
			allowed_alter="true"
			allowed_drop="true"
			allowed_revoke="true"
			allowed_create="true"
			allowed_grant="true";
	}

	private void function removeServerDatasource( required string name ) {
		admin
			action="removeDatasource"
			type="server"
			password="#variables.adminPassword#"
			name="#arguments.name#";
	}

	private void function removeServerDatasourceIfExists( required string name ) {
		try {
			removeServerDatasource( arguments.name );
		}
		catch ( any e ) {}
	}

	private boolean function isH2NotSupported() {
		try {
			var ds = server.getTestService( "h2", server._getTempDir( "LDEV5967" ) );
			return structIsEmpty( ds );
		}
		catch ( any e ) {
			return true;
		}
	}

}
