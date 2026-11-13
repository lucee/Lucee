component extends="org.lucee.cfml.test.LuceeTestCase" labels="postgres,mysql,h2,datasource" {

	function run( testResults, testBox ) {

		describe( "LDEV-4213 - a pooled connection that threw an SQLException must be checked before it is used again", function() {

			// the kill is done by a second datasource, which has its own pool, so we never kill the connection we use to kill
			it( title="postgres: a connection terminated by the server is not handed out again (validate=false)", skip=isNotAvailable( "postgres" ), body=function( currentSpec ) {
				testKilledConnection(
					service: "postgres",
					pidSQL: "select pg_backend_pid() as pid",
					killSQL: function( pid ) { return "select pg_terminate_backend( #pid# )"; }
				);
			});

			it( title="mysql: a connection killed by the server is not handed out again (validate=false)", skip=isNotAvailable( "mysql" ), body=function( currentSpec ) {
				testKilledConnection(
					service: "mysql",
					pidSQL: "select connection_id() as pid",
					killSQL: function( pid ) { return "kill #pid#"; }
				);
			});

			it( title="postgres: a SQL syntax error does not invalidate the connection (validate=false)", skip=isNotAvailable( "postgres" ), body=function( currentSpec ) {
				testSyntaxError( service: "postgres", pidSQL: "select pg_backend_pid() as pid" );
			});

			it( title="mysql: a SQL syntax error does not invalidate the connection (validate=false)", skip=isNotAvailable( "mysql" ), body=function( currentSpec ) {
				testSyntaxError( service: "mysql", pidSQL: "select connection_id() as pid" );
			});

			it( title="h2: a SQL syntax error does not invalidate the connection (validate=false)", body=function( currentSpec ) {
				testSyntaxError( service: "h2", pidSQL: "select session_id() as pid" );
			});

			it( title="postgres: the connection of a transaction is kept after a failed statement", skip=isNotAvailable( "postgres" ), body=function( currentSpec ) {
				var dsName = "LDEV4213_pg_tx";
				application action="update" datasources={ "#dsName#": getTestDatasource( "postgres" ) };
				var pidBefore = queryExecute( "select pg_backend_pid() as pid", [], { datasource: dsName } ).pid;
				var activeBefore = getPoolActive( dsName );
				var pidInTx = "";
				var pidInTx2 = "";
				transaction {
					pidInTx = queryExecute( "select pg_backend_pid() as pid", [], { datasource: dsName } ).pid;
					var failed = false;
					try {
						queryExecute( "selec 1", [], { datasource: dsName } );
					}
					catch ( database e ) {
						failed = true;
					}
					expect( failed ).toBeTrue( "the invalid SQL did not throw" );
					transactionRollback();
					// a plain SQL error must not take the connection away from the transaction
					pidInTx2 = queryExecute( "select pg_backend_pid() as pid", [], { datasource: dsName } ).pid;
					transactionCommit();
				}
				expect( pidInTx ).toBe( pidBefore );
				expect( pidInTx2 ).toBe( pidInTx, "the connection of the transaction was replaced after a SQL error" );
				expect( getPoolActive( dsName ) ).toBe( activeBefore );
				expect( queryExecute( "select pg_backend_pid() as pid", [], { datasource: dsName } ).pid ).toBe( pidBefore );
			});

		});

		describe( "LDEV-4213 - SQLStates that say the connection is gone", function() {

			var impl = createObject( "java", "lucee.runtime.db.DatasourceConnectionImpl" );

			it( title="connection exceptions (class 08) and authentication failures are connection errors", body=function( currentSpec ) {
				for ( var state in [ "08000", "08001", "08003", "08004", "08006", "08S01", "08P01", "28000", "28P01" ] ) {
					expect( impl.isConnectionError( createObject( "java", "java.sql.SQLException" ).init( "failed", state ) ) ).toBeTrue( "SQLState #state#" );
				}
			});

			it( title="other errors are not connection errors", body=function( currentSpec ) {
				// syntax error, unique violation, admin shutdown, other states
				for ( var state in [ "42000", "42601", "23505", "22012", "57P01", "40001", "HY000", "0A000", "2800" ] ) {
					expect( impl.isConnectionError( createObject( "java", "java.sql.SQLException" ).init( "failed", state ) ) ).toBeFalse( "SQLState [#state#]" );
				}
				expect( impl.isConnectionError( createObject( "java", "java.sql.SQLException" ).init( "failed" ) ) ).toBeFalse( "no SQLState" );
			});

			it( title="looks at the next exception and the cause", body=function( currentSpec ) {
				var SQLException = createObject( "java", "java.sql.SQLException" );
				var outer = SQLException.init( "outer", "42000" );
				outer.setNextException( SQLException.init( "next", "08006" ) );
				expect( impl.isConnectionError( outer ) ).toBeTrue( "next exception" );

				var withCause = SQLException.init( "outer", "HY000" );
				withCause.initCause( SQLException.init( "cause", "28P01" ) );
				expect( impl.isConnectionError( withCause ) ).toBeTrue( "cause" );

				var none = SQLException.init( "outer", "42000" );
				none.setNextException( SQLException.init( "next", "23505" ) );
				expect( impl.isConnectionError( none ) ).toBeFalse( "chain without a connection error" );
			});

		});
	}

	// kill the connection on the server, the driver does not know yet and with validate=false Lucee did not check it
	private void function testKilledConnection( required string service, required string pidSQL, required function killSQL ) {
		var dsName = "LDEV4213_#arguments.service#_kill";
		var killerName = "LDEV4213_#arguments.service#_killer";
		application action="update" datasources={
			"#dsName#": getTestDatasource( arguments.service ),
			"#killerName#": getTestDatasource( arguments.service )
		};

		// one idle connection in the pool
		var pidBefore = queryExecute( arguments.pidSQL, [], { datasource: dsName } ).pid;
		var activeBefore = getPoolActive( dsName );
		expect( queryExecute( arguments.pidSQL, [], { datasource: dsName } ).pid ).toBe( pidBefore, "precondition: the pool reuses the connection" );

		queryExecute( arguments.killSQL( pidBefore ), [], { datasource: killerName } );
		// give the server time to close the connection
		sleep( 1000 );

		// the pool does not know the connection is dead (validate=false), so this is the query that finds out
		var failed = false;
		try {
			queryExecute( arguments.pidSQL, [], { datasource: dsName } );
		}
		catch ( database e ) {
			failed = true;
		}
		expect( failed ).toBeTrue( "precondition: the killed connection was handed out and failed" );

		// the connection that failed must not be handed out again
		var res = queryExecute( arguments.pidSQL, [], { datasource: dsName } );
		expect( res.pid ).notToBe( pidBefore, "the connection killed by the server was handed out again" );
		expect( getPoolActive( dsName ) ).toBe( activeBefore, "the pool accounting is wrong after the failed connection" );

		// and the new one is reused as normal
		expect( queryExecute( arguments.pidSQL, [], { datasource: dsName } ).pid ).toBe( res.pid );
	}

	private void function testSyntaxError( required string service, required string pidSQL ) {
		var dsName = "LDEV4213_#arguments.service#_syntax";
		application action="update" datasources={ "#dsName#": getTestDatasource( arguments.service ) };

		var pidBefore = queryExecute( arguments.pidSQL, [], { datasource: dsName } ).pid;
		var activeBefore = getPoolActive( dsName );

		loop times=3 {
			var failed = false;
			try {
				queryExecute( "selec 1 frm nothing", [], { datasource: dsName } );
			}
			catch ( database e ) {
				failed = true;
			}
			expect( failed ).toBeTrue( "the invalid SQL did not throw" );
		}

		expect( queryExecute( arguments.pidSQL, [], { datasource: dsName } ).pid ).toBe( pidBefore, "a SQL syntax error replaced the connection" );
		expect( getPoolActive( dsName ) ).toBe( activeBefore );
	}

	private struct function getTestDatasource( required string service ) {
		var ds = {};
		if ( arguments.service == "h2" ) {
			ds = server.getDatasource( "h2", server._getTempDir( "LDEV4213-h2" ) );
		}
		else {
			ds = server.getDatasource( arguments.service );
		}
		ds.validate = false;
		return ds;
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

	private boolean function isNotAvailable( required string service ) {
		return structIsEmpty( server.getDatasource( arguments.service ) );
	}

}
