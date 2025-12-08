component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {

	/**
	 * LDEV-5961 - Connection pool issues after Lucee 6.2 upgrade
	 *
	 * Two issues identified:
	 * 1. maxTotal defaults to 60 when using connectionString style datasource definition,
	 *    which overrides the connectionLimit setting. The type/host style defaults to 0 (correct).
	 * 2. maxWaitMillis is infinite (-1), causing threads to block forever when pool exhausted
	 *
	 * Location of bug #1: AppListenerUtil.java line 236 - defaults maxTotal to 60 for connectionString style
	 * Location of bug #2: ConfigImpl.java line 2468 - passes 0 for maxWaitMillis (becomes -1 = infinite)
	 */

	function beforeAll() {
		if ( isMySqlNotSupported() ) return;

		variables.creds = mySqlCredentials( true );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-5961 - Connection Pool Configuration", function() {

			describe( "connectionLimit should be respected", function() {

				it( title="connectionString style: connectionLimit should limit pool when maxTotal not set", skip=isMySqlNotSupported(), body=function( currentSpec ) {
					// BUG: When using connectionString style, maxTotal defaults to 60,
					// which overrides connectionLimit. This is inconsistent with type/host style
					// which defaults maxTotal to 0 (allowing connectionLimit to work).
					var dsConfig = {
						class: "com.mysql.cj.jdbc.Driver",
						bundleName: "com.mysql.cj",
						connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
						username: creds.username,
						password: creds.password,
						connectionLimit: 2
						// maxTotal NOT set - defaults to 60 (BUG), should respect connectionLimit
					};

					application action="update" datasources={ "LDEV5961_connstr": dsConfig };

					// Get initial connection to create the pool
					query datasource="LDEV5961_connstr" name="local.q" { echo( "SELECT 1" ); }

					// Spawn 3 threads that hold connections for 1 second
					var threadCount = 3;
					var holdTime = 1;

					for ( var i = 1; i <= threadCount; i++ ) {
						thread name="LDEV5961_cs_#i#" holdSec=holdTime {
							try {
								query datasource="LDEV5961_connstr" name="local.q" {
									echo( "SELECT SLEEP(#holdSec#)" );
								}
								thread.success = true;
							}
							catch ( any e ) {
								thread.success = false;
								thread.error = e.message;
							}
						}
						sleep( 50 );
					}

					// Wait for threads to grab connections
					sleep( 200 );

					// Check metrics - should show only 2 active if connectionLimit is respected
					var metrics = getSystemMetrics();

					// Wait for threads to complete
					for ( var i = 1; i <= threadCount; i++ ) {
						thread action="join" name="LDEV5961_cs_#i#" timeout="#( holdTime + 3 ) * 1000#";
					}

					// With connectionLimit=2 and 3 threads:
					// EXPECTED: 2 active connections (respecting connectionLimit)
					// ACTUAL (BUG): 3 active connections (maxTotal=60 default overrides connectionLimit)
					systemOutput( "Test 1 metrics: " & serializeJSON( var=getSystemMetrics(), compact=false ), true );
					expect( metrics.activeDatasourceConnections ).toBeLTE( 2,
						"Pool should not exceed connectionLimit of 2, but maxTotal default of 60 is overriding it" );
				});

				it( title="type/host style: connectionLimit should work correctly", skip=isMySqlNotSupported(), body=function( currentSpec ) {
					// This style correctly defaults maxTotal to 0, allowing connectionLimit to work
					var dsConfig = {
						type: "mysql",
						host: creds.server,
						port: creds.port,
						database: creds.database,
						username: creds.username,
						password: creds.password,
						connectionLimit: 2
						// maxTotal NOT set - correctly defaults to 0, so connectionLimit is used
					};

					application action="update" datasources={ "LDEV5961_typehost": dsConfig };

					// Get initial connection to create the pool
					query datasource="LDEV5961_typehost" name="local.q" { echo( "SELECT 1" ); }

					// Spawn 3 threads
					var threadCount = 3;
					var holdTime = 1;

					for ( var i = 1; i <= threadCount; i++ ) {
						thread name="LDEV5961_th_#i#" holdSec=holdTime {
							try {
								query datasource="LDEV5961_typehost" name="local.q" {
									echo( "SELECT SLEEP(#holdSec#)" );
								}
								thread.success = true;
							}
							catch ( any e ) {
								thread.success = false;
								thread.error = e.message;
							}
						}
						sleep( 50 );
					}

					sleep( 200 );
					var metrics = getSystemMetrics();

					for ( var i = 1; i <= threadCount; i++ ) {
						thread action="join" name="LDEV5961_th_#i#" timeout="#( holdTime + 3 ) * 1000#";
					}

					// This should work correctly - connectionLimit=2 is respected
					systemOutput( "Test 2 metrics: " & serializeJSON( var=getSystemMetrics(), compact=false ), true );
					expect( metrics.activeDatasourceConnections ).toBeLTE( 2,
						"type/host style should respect connectionLimit" );
				});

				it( title="explicit maxTotal should override connectionLimit", skip=isMySqlNotSupported(), body=function( currentSpec ) {
					var dsConfig = {
						class: "com.mysql.cj.jdbc.Driver",
						bundleName: "com.mysql.cj",
						connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
						username: creds.username,
						password: creds.password,
						connectionLimit: 10,
						maxTotal: 2 // Explicit maxTotal should take precedence
					};

					application action="update" datasources={ "LDEV5961_maxtotal": dsConfig };

					// Spawn 3 threads
					for ( var i = 1; i <= 3; i++ ) {
						thread name="LDEV5961_mt_#i#" idx=i {
							try {
								query datasource="LDEV5961_maxtotal" name="local.q" {
									echo( "SELECT SLEEP(1)" );
								}
								thread.success = true;
							}
							catch ( any e ) {
								thread.success = false;
								thread.error = e.message;
							}
						}
						sleep( 50 );
					}

					sleep( 200 );
					var metrics = getSystemMetrics();

					// Wait for threads
					for ( var i = 1; i <= 3; i++ ) {
						thread action="join" name="LDEV5961_mt_#i#" timeout="5000";
					}

					// With explicit maxTotal=2, should be limited to 2 active
					systemOutput( "Test 3 metrics: " & serializeJSON( var=getSystemMetrics(), compact=false ), true );
					expect( metrics.activeDatasourceConnections ).toBeLTE( 2,
						"Explicit maxTotal=2 should limit pool to 2 connections" );
				});

			});

			describe( "pool exhaustion should timeout, not block forever", function() {

				it( title="should timeout when pool exhausted instead of blocking forever", skip=isMySqlNotSupported(), body=function( currentSpec ) {
					// BUG: maxWaitMillis is infinite (-1), so threads block forever when pool exhausted
					var dsConfig = {
						class: "com.mysql.cj.jdbc.Driver",
						bundleName: "com.mysql.cj",
						connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
						username: creds.username,
						password: creds.password,
						connectionLimit: 1,
						maxTotal: 1,
						connectionTimeout: 1 // 1 minute - should be used for maxWaitMillis but isn't
					};

					application action="update" datasources={ "LDEV5961_timeout": dsConfig };

					// First thread grabs the only connection and holds it for 5 seconds
					thread name="LDEV5961_holder2" {
						try {
							query datasource="LDEV5961_timeout" name="local.q" {
								echo( "SELECT SLEEP(5)" ); // Hold longer than test wait time
							}
						}
						catch( any e ) {
							// Connection may be killed during cleanup
						}
					}

					sleep( 500 ); // Let holder get the connection

					// Second thread tries to get a connection
					var startTime = getTickCount();
					thread name="LDEV5961_waiter2" {
						try {
							thread.beginTime = getTickCount();
							query datasource="LDEV5961_timeout" name="local.q" {
								echo( "SELECT 1" );
							}
							thread.elapsed = getTickCount() - thread.beginTime;
							thread.success = true;
						}
						catch ( any e ) {
							thread.elapsed = getTickCount() - thread.beginTime;
							thread.success = false;
							thread.error = e.message;
						}
					}

					// Wait max 2 seconds for waiter
					// With proper maxWaitMillis, waiter should timeout quickly with error
					// With bug (infinite wait), waiter will still be RUNNING
					thread action="join" name="LDEV5961_waiter2" timeout="2000";

					var waiterStatus = cfthread.LDEV5961_waiter2.status ?: "UNKNOWN";
					var elapsed = getTickCount() - startTime;

					// Clean up holder thread (don't wait long, just let it go)
					thread action="join" name="LDEV5961_holder2" timeout="100";

					// BUG: With infinite maxWaitMillis (-1), waiter blocks forever waiting for pool
					// With proper maxWaitMillis set, waiter should timeout quickly with an error
					//
					// Current bug: waiter thread is STILL RUNNING after 2 seconds because it's
					// blocked on borrowObject() with infinite wait time
					systemOutput( "Test 4 metrics: " & serializeJSON( var=getSystemMetrics(), compact=false ), true );
					systemOutput( "Test 4 waiterStatus: #waiterStatus#, elapsed: #elapsed#ms", true );
					if ( structKeyExists( cfthread.LDEV5961_waiter2, "error" ) ) {
						systemOutput( "Test 4 waiter error: #cfthread.LDEV5961_waiter2.error#", true );
					}
					expect( waiterStatus ).toBe( "TERMINATED",
						"Waiter thread status is [#waiterStatus#]. Expected TERMINATED (with pool timeout error). " &
						"If RUNNING, thread is blocked forever due to maxWaitMillis=-1 (infinite)." );
				});

			});

		});

	}

	function isMySqlNotSupported() {
		return isEmpty( mySqlCredentials() );
	}

	private struct function mySqlCredentials( onlyConfig=false ) {
		return server.getDatasource( service="mysql", onlyConfig=arguments.onlyConfig );
	}

}
