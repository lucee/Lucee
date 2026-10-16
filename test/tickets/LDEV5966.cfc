component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {

	/**
	 * LDEV-5966 - Request timeout closes connection instead of returning to pool
	 *
	 * When a request times out (via requesttimeout setting), the connection is
	 * closed directly via IOUtil.closeEL(dc) instead of being returned to the pool.
	 * This causes the pool's numActive counter to become out of sync with reality -
	 * the pool thinks the connection is still borrowed, but it's actually closed.
	 *
	 * Under high concurrency (e.g., 400 threads, 100 max connections), this leads
	 * to "phantom connections" - the pool reports connections as active but they
	 * don't exist on the database server. Eventually the pool thinks it's exhausted
	 * when it actually has capacity.
	 *
	 * Location: DatasourceManagerImpl.java lines 189-190
	 * if (pc != null && ((PageContextImpl) pc).getTimeoutStackTrace() != null) {
	 *     IOUtil.closeEL(dc);  // BUG: Should return to pool, not close directly
	 * }
	 */

	function beforeAll() {
		if ( isMySqlNotSupported() ) return;

		variables.creds = mySqlCredentials( true );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-5966 - Request timeout pool accounting", function() {

			// skip: test passes but is slow (~8s) - request timeout test
			it( title="request timeout should not leak pool connection count", skip=true, body=function( currentSpec ) {
				// This test verifies that when a request times out, the connection
				// is properly returned to the pool (or destroyed and accounted for),
				// not just closed leaving the pool's numActive counter inflated.

				var dsName = "LDEV5966_timeout";
				var dsConfig = {
					class: "com.mysql.cj.jdbc.Driver",
					bundleName: "com.mysql.cj",
					connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
					username: creds.username,
					password: creds.password,
					connectionLimit: 2,
					maxTotal: 2
				};

				application action="update" datasources={ "#dsName#": dsConfig };

				// Warm up the pool with a simple query
				query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }

				// Get initial pool metrics
				var metricsBefore = getSystemMetrics();
				var poolActiveBefore = getPoolActiveConnections( metricsBefore, dsName );
				systemOutput( "LDEV-5966: Before timeout test - poolActive=#poolActiveBefore#", true );

				// Spawn a thread that will timeout while holding a connection
				// The thread uses a 1-second requesttimeout but the query takes 5 seconds
				thread name="LDEV5966_timeout_thread" dsName=dsName {
					try {
						// Set a short request timeout
						setting requesttimeout="1";

						// This query will take longer than the timeout
						query datasource="#dsName#" name="local.q" {
							echo( "SELECT SLEEP(5)" );
						}
						thread.timedOut = false;
					}
					catch ( any e ) {
						thread.timedOut = true;
						thread.error = e.message;
					}
				}

				// Wait for the thread to timeout (should happen after ~1 second)
				thread action="join" name="LDEV5966_timeout_thread" timeout="3000";

				// Small delay for cleanup to happen
				sleep( 500 );

				// Check pool metrics after timeout
				var metricsAfter = getSystemMetrics();
				var poolActiveAfter = getPoolActiveConnections( metricsAfter, dsName );
				systemOutput( "LDEV-5966: After timeout - poolActive=#poolActiveAfter#", true );

				// The thread should have timed out
				if ( structKeyExists( cfthread.LDEV5966_timeout_thread, "timedOut" ) ) {
					systemOutput( "LDEV-5966: Thread timedOut=#cfthread.LDEV5966_timeout_thread.timedOut#", true );
				}
				if ( structKeyExists( cfthread.LDEV5966_timeout_thread, "error" ) ) {
					systemOutput( "LDEV-5966: Thread error=#cfthread.LDEV5966_timeout_thread.error#", true );
				}

				// Now try to use both connections - if pool accounting is broken,
				// the pool thinks a connection is still borrowed when it's not
				var canBorrowConnections = true;
				try {
					// These should work if pool is healthy
					thread name="LDEV5966_verify_1" dsName=dsName {
						query datasource="#dsName#" name="local.q" {
							echo( "SELECT SLEEP(2)" );
						}
					}
					thread name="LDEV5966_verify_2" dsName=dsName {
						query datasource="#dsName#" name="local.q" {
							echo( "SELECT SLEEP(2)" );
						}
					}

					sleep( 500 ); // Let threads start

					var metricsVerify = getSystemMetrics();
					var poolActiveVerify = getPoolActiveConnections( metricsVerify, dsName );
					systemOutput( "LDEV-5966: During verify - poolActive=#poolActiveVerify#", true );

					// Wait for verification threads
					thread action="join" name="LDEV5966_verify_1" timeout="5000";
					thread action="join" name="LDEV5966_verify_2" timeout="5000";
				}
				catch ( any e ) {
					canBorrowConnections = false;
					systemOutput( "LDEV-5966: Failed to borrow connections: #e.message#", true );
				}

				// Pool should be healthy - able to use all connections
				expect( canBorrowConnections ).toBeTrue(
					"Pool should be able to borrow all connections after timeout. " &
					"If this fails, the timeout leaked pool accounting (numActive inflated)." );

				// Pool active should return to 0 (or same as before) after all operations complete
				sleep( 500 );
				var metricsFinal = getSystemMetrics();
				var poolActiveFinal = getPoolActiveConnections( metricsFinal, dsName );
				systemOutput( "LDEV-5966: Final - poolActive=#poolActiveFinal#", true );

				// Ideally poolActive should be 0 or back to where it started
				// A leak would show poolActive > 0 with no actual connections borrowed
				expect( poolActiveFinal ).toBeLTE( poolActiveBefore,
					"Pool active count should not be higher than before test. " &
					"Before=#poolActiveBefore#, Final=#poolActiveFinal#. " &
					"If higher, request timeout leaked pool accounting." );
			});

			// skip: test passes but is slow (~8s) - burst timeout test
			it( title="multiple request timeouts should not accumulate phantom connections", skip=true, body=function( currentSpec ) {
				// This test simulates burst load with timeouts to catch accumulating leaks

				var dsName = "LDEV5966_burst";
				var dsConfig = {
					class: "com.mysql.cj.jdbc.Driver",
					bundleName: "com.mysql.cj",
					connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
					username: creds.username,
					password: creds.password,
					connectionLimit: 3,
					maxTotal: 3
				};

				application action="update" datasources={ "#dsName#": dsConfig };

				// Warm up pool
				query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }

				var metricsBefore = getSystemMetrics();
				var poolActiveBefore = getPoolActiveConnections( metricsBefore, dsName );
				systemOutput( "LDEV-5966 burst: Before - poolActive=#poolActiveBefore#", true );

				// Run 5 iterations of timeout-inducing queries
				// Each iteration should timeout and release (not leak) the connection
				for ( var i = 1; i <= 5; i++ ) {
					thread name="LDEV5966_burst_#i#" dsName=dsName iteration=i {
						try {
							setting requesttimeout="1";
							query datasource="#dsName#" name="local.q" {
								echo( "SELECT SLEEP(3)" );
							}
						}
						catch ( any e ) {
							// Expected timeout
						}
					}

					// Stagger the threads slightly
					sleep( 200 );
				}

				// Wait for all threads to timeout
				for ( var i = 1; i <= 5; i++ ) {
					thread action="join" name="LDEV5966_burst_#i#" timeout="5000";
				}

				// Give pool time to clean up
				sleep( 1000 );

				var metricsAfter = getSystemMetrics();
				var poolActiveAfter = getPoolActiveConnections( metricsAfter, dsName );
				systemOutput( "LDEV-5966 burst: After 5 timeouts - poolActive=#poolActiveAfter#", true );

				// If there's a leak, poolActive would be inflated (up to 5 phantom connections)
				// With proper handling, it should be back to 0 or near it
				expect( poolActiveAfter ).toBeLTE( 1,
					"Pool active count should be near 0 after all timeouts complete. " &
					"Actual=#poolActiveAfter#. If high, each timeout leaked pool accounting." );

				// Verify pool is still usable - can borrow all 3 connections
				var borrowed = 0;
				try {
					for ( var i = 1; i <= 3; i++ ) {
						thread name="LDEV5966_final_#i#" dsName=dsName {
							query datasource="#dsName#" name="local.q" {
								echo( "SELECT SLEEP(1)" );
							}
						}
					}
					sleep( 300 );
					var metricsDuring = getSystemMetrics();
					borrowed = getPoolActiveConnections( metricsDuring, dsName );
					systemOutput( "LDEV-5966 burst: Borrowed 3 connections - poolActive=#borrowed#", true );

					for ( var i = 1; i <= 3; i++ ) {
						thread action="join" name="LDEV5966_final_#i#" timeout="3000";
					}

					expect( borrowed ).toBeGTE( 2,
						"Should be able to borrow multiple connections after timeouts. " &
						"Only borrowed #borrowed#. Pool may be exhausted from phantom connections." );
				}
				catch ( any e ) {
					fail( "Could not borrow connections after timeout burst: #e.message#" );
				}
			});

		});

	}

	private numeric function getPoolActiveConnections( required struct metrics, required string dsName ) {
		if ( !structKeyExists( metrics, "datasourceConnections" ) ) {
			return 0;
		}
		for ( var key in metrics.datasourceConnections ) {
			var pool = metrics.datasourceConnections[ key ];
			if ( structKeyExists( pool, "name" ) && pool.name == arguments.dsName ) {
				return pool.activeDatasourceConnections ?: 0;
			}
		}
		return 0;
	}

	function isMySqlNotSupported() {
		return isEmpty( mySqlCredentials() );
	}

	private struct function mySqlCredentials( onlyConfig=false ) {
		return server.getDatasource( service="mysql", onlyConfig=arguments.onlyConfig );
	}

}
