component extends="org.lucee.cfml.test.LuceeTestCase" labels="postgres,datasource" {

	function beforeAll() {
		variables.dsName = "LDEV6503_pg";
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6503 - a connection used by a timed out request must not be returned to the pool", function() {

			it( title="the next borrower gets its own result on a fresh connection (validate=false)", skip=isPostgresNotSupported(), body=function( currentSpec ) {
				var ds = server.getDatasource( "postgres" );
				ds.validate = false;
				application action="update" datasources={ "#variables.dsName#": ds };

				// one idle connection in the pool, request A and the check below share it (LIFO)
				var pidBefore = queryExecute( "select pg_backend_pid() as pid", [], { datasource: variables.dsName } ).pid;
				var activeBefore = getPoolActive( variables.dsName );

				thread name="LDEV6503_A" dsName=variables.dsName {
					setting requesttimeout="1";
					// Java < 20: the forced stop hits once the result has arrived and leaves it unread on the connection
					// Java 20+: only the interrupt happens, the request still timed out while it used the connection
					query name="local.q" datasource=attributes.dsName {
						echo( "select 'AAAA' as a_marker, g as a_id from (select pg_sleep(5)) s, generate_series(1, 3) g" );
					}
					// only reached when the thread was not stopped
					var st = getPageContext().getTimeoutStackTrace();
					thread.timedOut = !isNull( st );
				}
				// the controller checks for request timeouts only every 5 seconds, so it can act on the timeout of A
				// after its query has finished (then nothing was aborted and the connection is rightly reused).
				// trigger the check ourselves while the query of A is still running
				sleep( 2000 );
				getPageContext().getCFMLFactory().checkTimeout();
				thread action="join" name="LDEV6503_A" timeout="15000";
				if ( structKeyExists( cfthread.LDEV6503_A, "timedOut" ) ) {
					expect( cfthread.LDEV6503_A.timedOut ).toBeTrue( "the request timeout of the thread was not acted on while its query was running" );
				}

				var res = queryExecute( "select pg_backend_pid() as pid", [], { datasource: variables.dsName } );
				expect( res.columnList ).toBe( "PID", "got the result of the query of the timed out request" );
				expect( res.pid ).notToBe( pidBefore, "the connection used by the timed out request was returned to the pool" );

				// LDEV-5966 the pool accounting must still be right
				expect( getPoolActive( variables.dsName ) ).toBe( activeBefore );
				var q = queryExecute( "select 'B' as b_marker, 42 as b_i", [], { datasource: variables.dsName } );
				expect( q.columnList ).toBe( "B_MARKER,B_I" );
				expect( q.b_i ).toBe( 42 );
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

	private boolean function isPostgresNotSupported() {
		return structIsEmpty( server.getDatasource( "postgres" ) );
	}

}
