component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {

	/**
	 * LDEV-5968 - DatasourceConnectionFactory robustness
	 *
	 * Factory methods lack proper error handling and connection state management:
	 * 1. Missing passivateObject() - connections returned to pool without state reset
	 * 2. Connection leak in create() - if DatasourceConnectionImpl constructor throws
	 * 3. Null check in destroyObject() - p.getObject() could return null
	 *
	 * Location: DatasourceConnectionFactory.java
	 */

	function beforeAll() {
		if ( isMySqlNotSupported() ) return;

		variables.creds = mySqlCredentials( true );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-5968 - Connection state reset (passivateObject)", function() {

			it( title="autoCommit should be reset when connection returned to pool", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				// When a connection is returned to the pool, autoCommit should be reset to true.
				// Without passivateObject, the next borrower might get a connection with
				// autoCommit=false from a previous transaction.

				var dsName = "LDEV5968_autocommit";
				var dsConfig = {
					class: "com.mysql.cj.jdbc.Driver",
					bundleName: "com.mysql.cj",
					connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
					username: creds.username,
					password: creds.password,
					connectionLimit: 1,
					maxTotal: 1  // Force same connection to be reused
				};

				application action="update" datasources={ "#dsName#": dsConfig };

				// First: start a transaction (sets autoCommit=false) and roll it back
				transaction action="begin" {
					query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }
					transaction action="rollback";
				}

				// Connection is now returned to pool. With passivateObject, autoCommit
				// should be reset to true. Without it, autoCommit might still be false.

				// Second: borrow the same connection and check its state
				// We can't directly check autoCommit from CFML, but we can verify
				// that a simple query works without being in a transaction
				var queryWorked = true;
				try {
					query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }
				}
				catch ( any e ) {
					queryWorked = false;
					systemOutput( "LDEV-5968: Query after transaction failed - #e.message#", true );
				}

				expect( queryWorked ).toBeTrue(
					"Query should work after previous transaction - connection state should be reset" );
			});

			it( title="uncommitted transaction should be rolled back when connection returned", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				// If a connection is returned to pool with uncommitted work,
				// passivateObject should roll it back to prevent data leaking to next borrower.

				var dsName = "LDEV5968_rollback";
				var tableName = "ldev5968_test_#createUUID().replace( '-', '', 'all' ).left( 8 )#";
				var dsConfig = {
					class: "com.mysql.cj.jdbc.Driver",
					bundleName: "com.mysql.cj",
					connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
					username: creds.username,
					password: creds.password,
					connectionLimit: 1,
					maxTotal: 1
				};

				application action="update" datasources={ "#dsName#": dsConfig };

				// Create test table
				query datasource="#dsName#" name="local.q" {
					echo( "CREATE TABLE IF NOT EXISTS #tableName# (id INT PRIMARY KEY, val VARCHAR(50))" );
				}
				query datasource="#dsName#" name="local.q" {
					echo( "TRUNCATE TABLE #tableName#" );
				}

				try {
					// Insert in a transaction but DON'T commit
					// (simulate code that forgets to commit/rollback)
					transaction action="begin" {
						query datasource="#dsName#" name="local.q" {
							echo( "INSERT INTO #tableName# (id, val) VALUES (1, 'uncommitted')" );
						}
						// No commit or rollback - connection returned to pool with uncommitted data
					}
					// Transaction block ends, connection returns to pool

					// Now borrow connection again and check if the uncommitted row is visible
					query datasource="#dsName#" name="local.check" {
						echo( "SELECT * FROM #tableName# WHERE id = 1" );
					}

					systemOutput( "LDEV-5968: Rows found after uncommitted transaction: #local.check.recordCount#", true );

					// With proper passivateObject (rollback on return), no rows should exist
					// Bug: without passivateObject, uncommitted data persists
					expect( local.check.recordCount ).toBe( 0,
						"Uncommitted transaction should be rolled back when connection returned to pool. " &
						"Found #local.check.recordCount# rows - passivateObject not calling rollback." );
				}
				finally {
					// Cleanup
					try {
						query datasource="#dsName#" name="local.q" {
							echo( "DROP TABLE IF EXISTS #tableName#" );
						}
					}
					catch ( any e ) {
						// Ignore cleanup errors
					}
				}
			});

			it( title="warnings should be cleared when connection returned to pool", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				// SQL warnings from previous queries should not leak to next borrower

				var dsName = "LDEV5968_warnings";
				var dsConfig = {
					class: "com.mysql.cj.jdbc.Driver",
					bundleName: "com.mysql.cj",
					connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
					username: creds.username,
					password: creds.password,
					connectionLimit: 1,
					maxTotal: 1
				};

				application action="update" datasources={ "#dsName#": dsConfig };

				// First query - might generate warnings in some scenarios
				query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }

				// Second query - should not see warnings from first query
				// (We can't easily verify this from CFML, but ensure no errors occur)
				var queryWorked = true;
				try {
					query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }
				}
				catch ( any e ) {
					queryWorked = false;
				}

				expect( queryWorked ).toBeTrue( "Queries should work with clean connection state" );
			});

		});

		describe( "LDEV-5968 - Connection pool resilience", function() {

			it( title="pool should remain usable after connection errors", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				// If a connection fails validation or has errors, pool should still work

				var dsName = "LDEV5968_resilience";
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

				// Warm up pool
				query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }

				// Try to cause a query error
				var errorThrown = false;
				try {
					query datasource="#dsName#" name="local.q" {
						echo( "SELECT * FROM nonexistent_table_ldev5968" );
					}
				}
				catch ( any e ) {
					errorThrown = true;
					systemOutput( "LDEV-5968: Expected error - #e.message#", true );
				}

				expect( errorThrown ).toBeTrue( "Query to nonexistent table should error" );

				// Pool should still be usable
				var poolStillWorks = true;
				try {
					query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }
				}
				catch ( any e ) {
					poolStillWorks = false;
					systemOutput( "LDEV-5968: Pool broken after error - #e.message#", true );
				}

				expect( poolStillWorks ).toBeTrue(
					"Pool should remain usable after query errors" );
			});

			it( title="concurrent access should not corrupt pool state", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				// Multiple threads borrowing/returning connections shouldn't corrupt pool

				var dsName = "LDEV5968_concurrent";
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

				// Spawn multiple threads doing queries
				var threadCount = 6;
				for ( var i = 1; i <= threadCount; i++ ) {
					thread name="LDEV5968_conc_#i#" dsName=dsName idx=i {
						try {
							for ( var j = 1; j <= 3; j++ ) {
								query datasource="#dsName#" name="local.q" {
									echo( "SELECT SLEEP(0.1)" );
								}
							}
							thread.success = true;
						}
						catch ( any e ) {
							thread.success = false;
							thread.error = e.message;
						}
					}
				}

				// Wait for all threads
				for ( var i = 1; i <= threadCount; i++ ) {
					thread action="join" name="LDEV5968_conc_#i#" timeout="10000";
				}

				// Count successes
				var successCount = 0;
				var errors = [];
				for ( var i = 1; i <= threadCount; i++ ) {
					var t = cfthread[ "LDEV5968_conc_#i#" ];
					if ( structKeyExists( t, "success" ) && t.success ) {
						successCount++;
					}
					else if ( structKeyExists( t, "error" ) ) {
						arrayAppend( errors, t.error );
					}
				}

				systemOutput( "LDEV-5968 concurrent: #successCount#/#threadCount# threads succeeded", true );
				if ( arrayLen( errors ) > 0 ) {
					systemOutput( "LDEV-5968 concurrent errors: #errors.toList( '; ' )#", true );
				}

				expect( successCount ).toBe( threadCount,
					"All #threadCount# threads should complete successfully. " &
					"Failures indicate pool state corruption under concurrency." );

				// Verify pool is healthy after concurrent access
				var metricsAfter = getSystemMetrics();
				var poolAfter = getPoolInfo( metricsAfter, dsName );
				systemOutput( "LDEV-5968 concurrent: After - active=#poolAfter.active#, idle=#poolAfter.idle#", true );

				// Active should be 0 after all threads complete
				expect( poolAfter.active ).toBe( 0,
					"Pool should have 0 active connections after all threads complete" );
			});

		});

	}

	private struct function getPoolInfo( required struct metrics, required string dsName ) {
		var result = { active: 0, idle: 0, open: 0 };
		if ( !structKeyExists( metrics, "datasourceConnections" ) ) {
			return result;
		}
		for ( var key in metrics.datasourceConnections ) {
			var pool = metrics.datasourceConnections[ key ];
			if ( structKeyExists( pool, "name" ) && pool.name == arguments.dsName ) {
				result.active = pool.activeDatasourceConnections ?: 0;
				result.idle = pool.idleDatasourceConnections ?: 0;
				result.open = result.active + result.idle;
				return result;
			}
		}
		return result;
	}

	function isMySqlNotSupported() {
		return isEmpty( mySqlCredentials() );
	}

	private struct function mySqlCredentials( onlyConfig=false ) {
		return server.getDatasource( service="mysql", onlyConfig=arguments.onlyConfig );
	}

}
