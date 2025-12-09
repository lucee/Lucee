component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {

	/**
	 * LDEV-5967 - Pool Lifecycle Management
	 *
	 * Connection pools are not properly closed when removed or on shutdown:
	 * 1. cleanConnectionPools() - removes pools from map but never calls close()
	 * 2. removeDatasourceConnectionPool() - uses clear() instead of close(), and has CME risk
	 * 3. No shutdown hook - pools never cleaned up when Lucee shuts down
	 *
	 * Location: ConfigImpl.java
	 */

	function beforeAll() {
		if ( isMySqlNotSupported() ) return;

		variables.creds = mySqlCredentials( true );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-5967 - Pool Lifecycle Management", function() {

			it( title="removing datasource should close pool connections", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				// When a datasource is removed via application update, its pool should be
				// properly closed (not just cleared). This ensures all connections are
				// destroyed and returned to the database.

				var dsName = "LDEV5967_remove";
				var dsConfig = {
					class: "com.mysql.cj.jdbc.Driver",
					bundleName: "com.mysql.cj",
					connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
					username: creds.username,
					password: creds.password,
					connectionLimit: 3,
					maxTotal: 3,
					minIdle: 2
				};

				// Create the datasource and warm up the pool
				application action="update" datasources={ "#dsName#": dsConfig };

				// Create some connections in the pool
				for ( var i = 1; i <= 3; i++ ) {
					query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }
				}

				// Check pool has connections
				var metricsBefore = getSystemMetrics();
				var poolBefore = getPoolInfo( metricsBefore, dsName );
				systemOutput( "LDEV-5967: Before removal - active=#poolBefore.active#, idle=#poolBefore.idle#, open=#poolBefore.open#", true );

				// Verify pool exists and has connections
				expect( poolBefore.open ).toBeGTE( 1, "Pool should have at least 1 connection before removal" );

				// Now remove the datasource by updating application with empty datasources
				// This triggers removeDatasourceConnectionPool()
				var currentDs = application.datasources ?: {};
				structDelete( currentDs, dsName );
				application action="update" datasources=currentDs;

				// Give time for cleanup
				sleep( 500 );

				// Check pool is gone
				var metricsAfter = getSystemMetrics();
				var poolAfter = getPoolInfo( metricsAfter, dsName );
				systemOutput( "LDEV-5967: After removal - active=#poolAfter.active#, idle=#poolAfter.idle#, open=#poolAfter.open#", true );

				// Pool should no longer exist or have 0 connections
				expect( poolAfter.open ).toBe( 0,
					"Pool connections should be 0 after datasource removal. " &
					"If not 0, pool.close() was not called (only clear() or nothing)." );
			});

			it( title="removing datasource while iterating pools should not throw ConcurrentModificationException", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				// Bug: removeDatasourceConnectionPool() iterates over pools.entrySet() and calls
				// pools.remove() inside the loop. Even with ConcurrentHashMap, modifying during
				// iteration can cause ConcurrentModificationException or skip entries.
				// Fix: collect keys to remove first, then remove after iteration completes.

				var dsNames = [ "LDEV5967_multi_1", "LDEV5967_multi_2", "LDEV5967_multi_3" ];
				var datasources = {};

				// Create multiple datasources
				for ( var dsName in dsNames ) {
					datasources[ dsName ] = {
						class: "com.mysql.cj.jdbc.Driver",
						bundleName: "com.mysql.cj",
						connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
						username: creds.username,
						password: creds.password,
						connectionLimit: 2,
						maxTotal: 2
					};
				}

				application action="update" datasources=datasources;

				// Use each datasource to create pools
				for ( var dsName in dsNames ) {
					query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }
				}

				// Verify pools exist
				var metricsBefore = getSystemMetrics();
				for ( var dsName in dsNames ) {
					var poolInfo = getPoolInfo( metricsBefore, dsName );
					systemOutput( "LDEV-5967 multi: #dsName# before - open=#poolInfo.open#", true );
				}

				// Remove all datasources at once - this could trigger CME if iteration is unsafe
				var noException = true;
				try {
					application action="update" datasources={};
				}
				catch ( any e ) {
					noException = false;
					systemOutput( "LDEV-5967 multi: Exception during removal - #e.message#", true );
				}

				expect( noException ).toBeTrue(
					"Removing multiple datasources should not throw ConcurrentModificationException" );

				// Verify all pools are cleaned up
				sleep( 500 );
				var metricsAfter = getSystemMetrics();
				for ( var dsName in dsNames ) {
					var poolInfo = getPoolInfo( metricsAfter, dsName );
					expect( poolInfo.open ).toBe( 0,
						"Pool #dsName# should have 0 connections after removal" );
				}
			});

			it( title="idle pool cleanup should close pools properly", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				// Tests cleanConnectionPools() - pools that are idle too long should be closed
				// Bug: cleanConnectionPools() removes from map but never calls close()

				var dsName = "LDEV5967_idle";
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

				// Use the datasource
				query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }

				var metricsBefore = getSystemMetrics();
				var poolBefore = getPoolInfo( metricsBefore, dsName );
				systemOutput( "LDEV-5967 idle: Before - open=#poolBefore.open#", true );

				// The pool exists
				expect( poolBefore.open ).toBeGTE( 1, "Pool should exist after query" );

				// Note: We can't easily test the 10-minute idle timeout in a unit test,
				// but we can verify the pool state is tracked correctly.
				// The actual fix ensures close() is called when cleanConnectionPools removes a pool.
			});

			it( title="pool should track connection state accurately", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				// Verifies pool metrics are accurate - precondition for other tests

				var dsName = "LDEV5967_metrics";
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

				// Start with no connections
				var metrics0 = getSystemMetrics();
				var pool0 = getPoolInfo( metrics0, dsName );

				// Use a connection
				query datasource="#dsName#" name="local.q" { echo( "SELECT 1" ); }

				var metrics1 = getSystemMetrics();
				var pool1 = getPoolInfo( metrics1, dsName );
				systemOutput( "LDEV-5967 metrics: After 1 query - active=#pool1.active#, idle=#pool1.idle#, open=#pool1.open#", true );

				// Should have at least 1 connection (either active or idle)
				expect( pool1.open ).toBeGTE( 1, "Pool should have at least 1 connection after query" );

				// Hold a connection active
				thread name="LDEV5967_metrics_holder" dsName=dsName {
					query datasource="#dsName#" name="local.q" {
						echo( "SELECT SLEEP(2)" );
					}
				}

				sleep( 300 );

				var metrics2 = getSystemMetrics();
				var pool2 = getPoolInfo( metrics2, dsName );
				systemOutput( "LDEV-5967 metrics: During held connection - active=#pool2.active#, idle=#pool2.idle#, open=#pool2.open#", true );

				// Should have 1 active connection
				expect( pool2.active ).toBeGTE( 1, "Pool should show 1 active connection while query running" );

				thread action="join" name="LDEV5967_metrics_holder" timeout="5000";

				sleep( 200 );

				var metrics3 = getSystemMetrics();
				var pool3 = getPoolInfo( metrics3, dsName );
				systemOutput( "LDEV-5967 metrics: After release - active=#pool3.active#, idle=#pool3.idle#, open=#pool3.open#", true );

				// Active should be back to 0
				expect( pool3.active ).toBe( 0, "Pool should show 0 active after connection released" );
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
