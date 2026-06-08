component extends="org.lucee.cfml.test.LuceeTestCase" labels="oracle" {

	variables.PKG = "ldev6307_pkg";
	variables.NO_ARG_PROC = "no_arg_flush_proc";
	variables.NAMED_PROC = "named_flush_proc";

	function beforeAll() {
		if ( notHasOracle() ) return;

		// avoid ORA-01882 timezone region not found, see LDEV0637/LDEV1147 pattern
		// stash + restore (afterAll) so the JVM-wide mutation doesn't leak into other tests
		var tz = getTimeZone();
		variables.originalTz = tz.getDefault();
		tz.setDefault( tz );

		application action="update" datasource="#server.getDatasource( 'oracle' )#";
	}

	function afterAll() {
		if ( notHasOracle() ) return;
		getTimeZone().setDefault( variables.originalTz );
	}

	function run() {
		describe( title="datasourceFlushMetaCache — pool-scoped cache + resolver-aware flush (LDEV-6307)", skip=notHasOracle(), body=function() {

			it( title="no-arg flush clears cache reached via the application's default datasource", body=function() {
				// warm cache with 13-cursor signature against the app default DS
				createCursorProc( variables.PKG, variables.NO_ARG_PROC, 13 );
				callCursorProc( variables.PKG, variables.NO_ARG_PROC, 13 );

				// recompile to 16 cursors
				createCursorProc( variables.PKG, variables.NO_ARG_PROC, 16 );

				// pre-flush call — cache stale, expects 13, proc has 16 → PLS-00306
				var thrown = "";
				try {
					callCursorProc( variables.PKG, variables.NO_ARG_PROC, 16 );
				}
				catch ( any e ) {
					thrown = e.message;
				}
				expect( thrown ).toInclude( "PLS-00306" );

				// no-arg flush walks the canonical pool registry; reaches the
				// app-singular __default__ DS that scopes the application
				expect( datasourceFlushMetaCache() ).toBeTrue();

				// post-flush call refetches metadata, binds 16 cursors, succeeds
				var results = callCursorProc( variables.PKG, variables.NO_ARG_PROC, 16 );
				for ( var i = 1; i <= 16; i++ ) {
					expect( results[ "r#i#" ] ).toBeQuery();
					expect( results[ "r#i#" ].n[ 1 ] ).toBe( i );
				}
			});

			it( title="named flush resolves via app-plural DS — symmetric with cfstoredproc datasource=", body=function() {
				// register a plural-form app-level DS named "oracle" so pc.getDataSource("oracle") resolves to it
				application action="update" datasources={ oracle: server.getDatasource( "oracle" ) };

				// warm against the named DS with a 13-cursor signature
				createCursorProc( variables.PKG, variables.NAMED_PROC, 13 );
				var warmup = {};
				storedproc datasource="oracle" procedure="#variables.PKG#.#variables.NAMED_PROC#" {
					for ( var i = 1; i <= 13; i++ ) {
						procresult name="warmup.r#i#" resultset=i;
					}
				}

				// recompile to 16
				createCursorProc( variables.PKG, variables.NAMED_PROC, 16 );

				// pre-flush named call should fail with stale-cache PLS-00306
				var thrown = "";
				try {
					var pre = {};
					storedproc datasource="oracle" procedure="#variables.PKG#.#variables.NAMED_PROC#" {
						for ( var i = 1; i <= 16; i++ ) {
							procresult name="pre.r#i#" resultset=i;
						}
					}
				}
				catch ( any e ) {
					thrown = e.message;
				}
				expect( thrown ).toInclude( "PLS-00306" );

				// named flush — pc.getDataSource("oracle") resolves to the app-plural DS,
				// matching pools by content-id clears that pool's cache
				expect( datasourceFlushMetaCache( "oracle" ) ).toBeTrue();

				// post-flush succeeds with 16 cursors
				var out = {};
				storedproc datasource="oracle" procedure="#variables.PKG#.#variables.NAMED_PROC#" {
					for ( var i = 1; i <= 16; i++ ) {
						procresult name="out.r#i#" resultset=i;
					}
				}
				for ( var i = 1; i <= 16; i++ ) {
					expect( out[ "r#i#" ] ).toBeQuery();
					expect( out[ "r#i#" ].n[ 1 ] ).toBe( i );
				}
			});

		});
	}

	private boolean function notHasOracle() {
		return structIsEmpty( server.getDatasource( "oracle" ) );
	}

	private void function createCursorProc( required string pkg, required string proc, required numeric cursorCount ) {
		var cursorParams = [];
		var cursorOpens = [];
		for ( var i = 1; i <= arguments.cursorCount; i++ ) {
			cursorParams.append( "p_cur#i# OUT sys_refcursor" );
			cursorOpens.append( "OPEN p_cur#i# FOR SELECT #i# AS n FROM DUAL;" );
		}
		var sig = cursorParams.toList( ", " );

		query {
			echo( "
				CREATE OR REPLACE package #arguments.pkg# as
					PROCEDURE #arguments.proc#( #sig# );
				END;
			" );
		}

		query {
			echo( "
				CREATE OR REPLACE package body #arguments.pkg# as
					PROCEDURE #arguments.proc#( #sig# ) IS
					BEGIN
						#cursorOpens.toList( ' ' )#
					END;
				END;
			" );
		}
	}

	private struct function callCursorProc( required string pkg, required string proc, required numeric cursorCount ) {
		var out = {};
		storedproc procedure="#arguments.pkg#.#arguments.proc#" {
			for ( var i = 1; i <= arguments.cursorCount; i++ ) {
				procresult name="out.r#i#" resultset=i;
			}
		}
		return out;
	}

}
