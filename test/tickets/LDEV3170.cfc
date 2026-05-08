component extends="org.lucee.cfml.test.LuceeTestCase" labels="oracle" {

	variables.CURSOR_COUNT = 16;
	variables.PKG = "ldev3170_pkg";
	variables.PROC = "multi_cursor_proc";

	function beforeAll() {
		if ( notHasOracle() ) return;

		// avoid ORA-01882 timezone region not found, see LDEV0637/LDEV1147 pattern
		// stash + restore (afterAll) so the JVM-wide mutation doesn't leak into other tests
		var tz = getTimeZone();
		variables.originalTz = tz.getDefault();
		tz.setDefault( tz );

		application action="update" datasource="#server.getDatasource( 'oracle' )#";

		createCursorProc( variables.PKG, variables.PROC, variables.CURSOR_COUNT );
	}

	function afterAll() {
		if ( notHasOracle() ) return;
		getTimeZone().setDefault( variables.originalTz );
	}

	function run() {
		describe( title="LDEV-3170 cfstoredproc with multiple Oracle ref cursors", skip=notHasOracle(), body=function() {

			it( title="binds and reads all #variables.CURSOR_COUNT# ref cursors", body=function() {
				var results = callCursorProc( variables.PKG, variables.PROC, variables.CURSOR_COUNT );
				for ( var i = 1; i <= variables.CURSOR_COUNT; i++ ) {
					expect( results[ "r#i#" ] ).toBeQuery();
					expect( results[ "r#i#" ].recordcount ).toBe( 1 );
					expect( results[ "r#i#" ].n[ 1 ] ).toBe( i );
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
