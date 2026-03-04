component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function run( testResults, testBox ) {
		runSuite( testResults, testBox, basicConfig() );
		runSuite( testResults, testBox, customConfig() );
		runCustomSuite( testResults, testBox, customConfig() );
	}

	private void function runSuite( testResults, testBox, required struct cfg ) {

		describe( "LDEV-6129 [#cfg.label#] - ORM connection not released when flushAll() throws at request end", function() {

			/*
			 * Bug: PageContextImpl.releaseORM():
			 *
			 *   try {
			 *       ormSession.flushAll(pc);    // throws constraint violation
			 *       ormSession.closeAll(pc);    // skipped — same try block
			 *       manager.releaseORM();
			 *   } finally {
			 *       ormSession = null;          // ref dropped, dc never returned to pool
			 *   }
			 *
			 * Pool is maxTotal=1. If the connection leaks after the flush error,
			 * the subsequent simple.cfm requests will fail to get a connection.
			 */
			it( title="connection returned to pool even when auto-flush throws a constraint violation", body=function( currentSpec ) {

				_InternalRequest( template: "#cfg.uri#/setup.cfm", url: cfg.params );

				// Trigger the potential leak: unique constraint violation at request end
				try {
					_InternalRequest( template: "#cfg.uri#/flush_leak.cfm", url: cfg.params );
				} catch ( any e ) {
					// _InternalRequest may propagate template exceptions — that's fine,
					// the important thing is what happens to the connection afterwards
					systemOutput( "flush_leak threw: #e.stacktrace#", true );
				}

				var metrics = getSystemMetrics();
				var active = getPoolActive( metrics, "LDEV6129h2" );
				var idle   = getPoolIdle( metrics, "LDEV6129h2" );
				systemOutput( "[#cfg.label#] after flush error: active=#active#, idle=#idle#", true );

				expect( active ).toBe( 0,
					"Connection leaked after flush error — active=#active# (pool maxTotal=1)"
				);

				// Now verify the connection is actually usable: make N simple requests.
				// If the connection was leaked (active but not in pool), these will fail.
				var N = 5;
				for ( var i = 1; i <= N; i++ ) {
					var result = _InternalRequest( template: "#cfg.uri#/simple.cfm", url: cfg.params );
					systemOutput( "[#cfg.label#] simple request #i#: status=#result.status#, content=#trim( result.filecontent )#", true );
					expect( result.status ).toBe( 200,
						"simple request #i# failed — connection not available (pool exhausted?)"
					);
					expect( left( trim( result.filecontent ), 2 ) ).toBe( "ok",
						"simple request #i# returned unexpected content: #trim( result.filecontent )#"
					);
				}

			} );

		} );

		describe( "LDEV-6129 [#cfg.label#] - dead reconnect code throws when session.isConnected() returns false", function() {

			/*
			 * Bug: HibernateORMSession.getSessionAndConn() has a dead reconnect block:
			 *
			 *   if ( !s.isOpen() || !s.isConnected() || isClosed( s ) ) {
			 *       sac.connect( pc );           // acquires dc from pool
			 *       s.reconnect( sac.getConnection( pc ) );  // ALWAYS throws IllegalStateException
			 *   }                                             // dc is leaked
			 *
			 * Session.reconnect(Connection) is not supported for factory-opened sessions
			 * in Hibernate 5.6 — it unconditionally throws IllegalStateException.
			 *
			 * Fix: remove the reconnect block. ConnectionProvider handles the lifecycle.
			 */
			it( title="entityLoad succeeds when session isConnected() is forced false via reflection", body=function( currentSpec ) {

				_InternalRequest( template: "#cfg.uri#/setup.cfm", url: cfg.params );

				var result = _InternalRequest( template: "#cfg.uri#/reconnect_leak.cfm", url: cfg.params );
				systemOutput( "[#cfg.label#] reconnect_leak result: status=#result.status#, content=#trim( result.filecontent )#", true );

				expect( result.status ).toBe( 200 );
				expect( left( trim( result.filecontent ), 2 ) ).toBe( "ok",
					"entityLoad failed after isConnected()=false — reconnect dead code is broken: #trim( result.filecontent )#"
				);

			} );

		} );

	}

	private void function runCustomSuite( testResults, testBox, required struct cfg ) {

		describe( "LDEV-6129 [#cfg.label#] - dead reconnect code triggered naturally by after_transaction release mode", function() {

			/*
			 * With connection.release_mode=after_transaction, Hibernate calls afterTransaction()
			 * after every ormFlush(), setting physicalConnection=null → isConnected()=false.
			 * The next ORM call then enters the dead reconnect block in getSessionAndConn(),
			 * which calls s.reconnect() — always throws ResourceClosedException in Hibernate 5.6.
			 */
			it( title="entityLoad succeeds after ormFlush() with after_transaction release mode", body=function( currentSpec ) {

				_InternalRequest( template: "#cfg.uri#/setup.cfm", url: { flushAtRequestEnd: false } );

				var result = _InternalRequest( template: "#cfg.uri#/multi_transaction.cfm", url: { flushAtRequestEnd: false } );
				systemOutput( "[#cfg.label#] multi_transaction result: status=#result.status#, content=#trim( result.filecontent )#", true );

				expect( result.status ).toBe( 200 );
				expect( left( trim( result.filecontent ), 2 ) ).toBe( "ok",
					"entityLoad failed after ormFlush() with after_transaction — dead reconnect code triggered: #trim( result.filecontent )#"
				);

			} );

		} );

	}

	private struct function basicConfig() {
		return { label: "basic", uri: createURI( "LDEV6129/basic" ), params: {} };
	}

	private struct function customConfig() {
		return { label: "custom (after_transaction)", uri: createURI( "LDEV6129/custom" ), params: { flushAtRequestEnd: true } };
	}

	private numeric function getPoolActive( required struct metrics, required string dsName ) {
		return getPoolStat( arguments.metrics, arguments.dsName, "activeDatasourceConnections" );
	}

	private numeric function getPoolIdle( required struct metrics, required string dsName ) {
		return getPoolStat( arguments.metrics, arguments.dsName, "idleDatasourceConnections" );
	}

	private numeric function getPoolStat( required struct metrics, required string dsName, required string stat ) {
		if ( !structKeyExists( arguments.metrics, "datasourceConnections" ) ) return 0;
		for ( var key in arguments.metrics.datasourceConnections ) {
			var pool = arguments.metrics.datasourceConnections[ key ];
			if ( structKeyExists( pool, "name" ) && pool.name == arguments.dsName ) {
				return val( pool[ arguments.stat ] ?: 0 );
			}
		}
		return 0;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
