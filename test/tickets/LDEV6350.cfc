/*
 * LDEV-6350: the spooler thread of the old engine was not stopped on an engine reset (restart, .lco update).
 * It kept waiting for its next task and held on to the old engine's classes (same leak as LDEV-6348).
 * A full restart can't be done inside the test run, so this tests the stop hook the engine reset uses:
 * stopping the spooler ends its thread, and the next spooled task starts a new one (nothing is lost).
 * No mail server needed (future sendTime only).
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function beforeAll() {
		variables.engine = getPageContext().getConfig().getSpoolerEngine();
		variables.subjects = [];
	}

	function afterAll() {
		var tasks = variables.engine.getOpenTasksAsQuery( 1, 10000 );
		for ( var row in tasks ) {
			if ( arrayFind( variables.subjects, row.name ) ) variables.engine.remove( row.id );
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6350 spooler thread on engine reset", function() {

			it( title="control: a spooled task starts the spooler thread, which waits for it", body=function( currentSpec ) {
				spool();
				var t = spoolerThread();
				expect( isNull( t ) ).toBeFalse( "no spooler thread" );
				expect( t.isAlive() ).toBeTrue( "spooler thread is not running" );
			});

			it( title="the spooler can be stopped (as on an engine reset), and the next spooled task starts it again", body=function( currentSpec ) {
				spool();
				var t = spoolerThread();
				expect( isNull( t ) ).toBeFalse( "no spooler thread" );
				expect( t.isAlive() ).toBeTrue( "spooler thread is not running" );

				var hasStop = true;
				try {
					variables.engine.stop();
				}
				catch ( e ) {
					hasStop = false;
					systemOutput( "LDEV6350 stop(): " & e.message, true );
				}
				expect( hasStop ).toBeTrue( "SpoolerEngineImpl has no stop(), so an engine reset can't end its thread" );

				var end = getTickCount() + 10000;
				while ( t.isAlive() && getTickCount() < end ) sleep( 50 );
				expect( t.isAlive() ).toBeFalse( "the spooler thread is still running 10s after stop()" );

				// the tasks are still there and the next one starts a new spooler thread
				spool();
				var t2 = spoolerThread();
				expect( isNull( t2 ) ).toBeFalse( "no spooler thread after the next spooled task" );
				expect( t2.isAlive() ).toBeTrue( "spooler thread not started again after stop()" );
				var tasks = variables.engine.getOpenTasksAsQuery( 1, 10000 );
				var mine = queryFilter( tasks, function( row ) { return arrayFind( variables.subjects, row.name ) > 0; } );
				expect( mine.recordCount ).toBe( arrayLen( variables.subjects ), "spooled tasks lost after stop()" );
			});
		});
	}

	private function spool() {
		var subject = "LDEV6350-" & createUUID();
		arrayAppend( variables.subjects, subject );
		mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject server="127.0.0.1" port="1"
				spoolEnable=true sendTime=dateAdd( "d", 1, now() ) {
			echo( "LDEV-6350" );
		}
		// the spooler thread is started right away, give it a moment to read the task and wait
		sleep( 200 );
	}

	// the engine's current spooler thread (private field), or null
	private function spoolerThread() {
		var f = variables.engine.getClass().getDeclaredField( "thread" );
		f.setAccessible( true );
		return f.get( variables.engine );
	}
}
