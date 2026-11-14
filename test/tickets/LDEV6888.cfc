/*
 * LDEV-6888: SpoolerEngineImpl deletes a spooled task file that is still being written
 *            (java.io.EOFException in SpoolerEngineImpl.getTask -> spooled cfmail silently lost)
 *
 * SpoolerEngineImpl.store() serializes a task straight into its final open/NNNNNNNN.tsk name, so
 * the file is visible to the SpoolerThread while it is still incomplete. SpoolerThread.run() lists
 * open/ and calls getTaskByName() -> getTask() with no coordination with the writer, and getTask()
 * DELETES the file on any read error. When the reader catches up with a writer that is slower than
 * it (cloud/network block storage, dirty-page throttling, a loaded host, a large HTML body), it hits
 * EOF, logs the EOFException at ERROR and removes the file. The writer carries on into the unlinked
 * inode, store() returns true, and the mail is never sent, retried, or moved to closed/.
 *
 * Both specs are deterministic and need no SMTP server: mail is spooled to a port nothing listens on,
 * so delivery fails and a genuine serialized MailSpoolerTask stays in open/ awaiting its retry. That
 * file is then used as the payload a concurrent writer is producing.
 *
 * Expected after a fix: no EOFException is logged and no task file disappears while being written.
 *
 * The first two specs are regression tests for LDEV-6489 (atomic write, unreadable files moved aside). The third covers
 * the related defect from the ticket: execute/remove by id and removeAll() never looked into closed/.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	variables.deadPort = 30259; // nothing must listen here
	variables.from     = "susi@sorglos.de";
	variables.to       = "geisse@peter.ch";

	function run( testResults, testBox ) {
		describe( title = "LDEV-6888 spooler must not destroy a task file that is still being written", body = function() {

			it( title = "a task file being written slowly (as store() does on slow storage) survives a concurrent spooler pass", body = function( currentSpec ) {
				lock name = "test:LDEV6888" timeout = 120 {
					var before  = listOpenTasks();
					var bytes   = createSeedTask( before, 512 );       // ~0.5 MB genuine MailSpoolerTask
					var target  = openDir() & "/99999992.tsk";
					var eofBefore = countSpoolerEOF();

					try {
						// writer: the exact bytes store() produces, streamed in 8 KB chunks (BufferedOutputStream size)
						thread name = "LDEV6888_slowWriter" action = "run" target = target bytes = bytes {
							var Arrays = createObject( "java", "java.util.Arrays" );
							var out    = createObject( "java", "java.io.FileOutputStream" ).init( attributes.target );
							try {
								for ( var pos = 0; pos < arrayLen( attributes.bytes ); pos += 8192 ) {
									out.write( Arrays.copyOfRange( attributes.bytes, javaCast( "int", pos ), javaCast( "int", min( pos + 8192, arrayLen( attributes.bytes ) ) ) ) );
									out.flush();
									sleep( 25 );
								}
							}
							finally {
								out.close();
							}
						}

						// while the file is visible but incomplete, wake the spooler (any add() interrupts it)
						sleep( 200 );
						spoolMail( "LDEV6888 trigger", "trigger" );
						thread action = "join" name = "LDEV6888_slowWriter" timeout = 120000;

						expect( fileExists( target ) ).toBeTrue( "the spooler deleted a task file while it was still being written - the task is lost" );
						expect( countSpoolerEOF() - eofBefore ).toBe( 0, "the spooler read a partially written task file (EOFException logged)" );
					}
					finally {
						cleanup( before );
					}
				}
			});

			it( title = "a partially written / not-yet-flushed task file is not deleted by the spooler", body = function( currentSpec ) {
				lock name = "test:LDEV6888" timeout = 120 {
					var before = listOpenTasks();
					try {
						var bytes     = createSeedTask( before, 64 );
						var half      = createObject( "java", "java.util.Arrays" ).copyOf( bytes, javaCast( "int", int( arrayLen( bytes ) / 2 ) ) );
						var halfFile  = openDir() & "/99999990.tsk";
						var emptyFile = openDir() & "/99999991.tsk";
						fileWrite( halfFile, half ); // what a reader sees mid-write of a large task
						fileWrite( emptyFile, "" );  // what a reader sees before a small task's first flush

						spoolMail( "LDEV6888 trigger", "trigger" );
						sleep( 5000 );

						// a reader that cannot tell "incomplete" from "corrupt" must not destroy the file
						expect( fileExists( halfFile ) ).toBeTrue( "half-written task file was deleted by the spooler" );
						expect( fileExists( emptyFile ) ).toBeTrue( "empty (not yet flushed) task file was deleted by the spooler" );
					}
					finally {
						cleanup( before );
					}
				}
			});

			it( title = "a task in the closed directory can be executed by id", body = function( currentSpec ) {
				lock name = "test:LDEV6888" timeout = 120 {
					var before = listOpenTasks();
					var closedFile = "";
					try {
						// sendTime is a day out, so the task is only stored (no delivery attempt, no mail server needed)
						mail to = variables.to from = variables.from subject = "LDEV6888 closed" type = "html"
								spoolEnable = true server = "localhost" port = variables.deadPort sendTime = dateAdd( "d", 1, now() ) {
							echo( "closed" );
						}
						var task = "";
						for ( var f in listOpenTasks() ) if ( !arrayFind( before, f ) ) task = f;
						expect( task ).notToBeEmpty( "the spooled task was not stored in open/" );

						// move it to closed/, the way a finished task is kept
						var closedDir = expandPath( "{lucee-config}/remote-client/closed" );
						if ( !directoryExists( closedDir ) ) directoryCreate( closedDir );
						closedFile = closedDir & "/" & task;
						fileMove( openDir() & "/" & task, closedFile );

						// executing it by id must find it in closed/ and run it. The delivery goes to a port nothing
						// listens on, so the run fails with an exception. Before the fix the task was never found
						// (open/ was searched twice), execute returned without doing anything.
						var failed = false;
						try {
							admin action = "executeSpoolerTask" type = "web" password = server.WEBADMINPASSWORD id = listFirst( task, "." );
						}
						catch ( any e ) {
							failed = true;
						}
						expect( failed ).toBeTrue( "executeSpoolerTask did not find the task in the closed directory" );
					}
					finally {
						if ( len( closedFile ) && fileExists( closedFile ) ) fileDelete( closedFile );
						cleanup( before );
					}
				}
			});
		});
	}

	// ---- helpers -------------------------------------------------------------------------------

	private string function openDir() {
		return expandPath( "{lucee-config}/remote-client/open" );
	}

	private array function listOpenTasks() {
		return directoryExists( openDir() ) ? directoryList( openDir(), false, "name", "*.tsk" ) : [];
	}

	private void function spoolMail( required string subject, required string body ) {
		mail to = variables.to from = variables.from subject = arguments.subject type = "html"
				spoolEnable = true server = "localhost" port = variables.deadPort {
			echo( arguments.body );
		}
	}

	// spools a mail that cannot be delivered and returns the bytes of its persisted task file
	private binary function createSeedTask( required array before, required numeric bodyKB ) {
		var row = '<tr><td style="padding:4px;border:1px solid ##ccc">Lorem ipsum dolor sit amet</td><td>12345.67</td></tr>' & chr( 10 );
		spoolMail( "LDEV6888 seed", "<html><body><table>" & repeatString( row, ceiling( arguments.bodyKB * 1024 / len( row ) ) ) & "</table></body></html>" );
		var seed = "";
		for ( var w = 0; w < 30 && !len( seed ); w++ ) {
			sleep( 1000 );
			for ( var f in listOpenTasks() ) if ( !arrayFind( arguments.before, f ) ) seed = f;
		}
		if ( !len( seed ) ) throw( message = "seed task never appeared in #openDir()#" );
		sleep( 3000 ); // let the first (failing) delivery attempt finish and re-store the file
		return fileReadBinary( openDir() & "/" & seed );
	}

	private void function cleanup( required array before ) {
		for ( var f in listOpenTasks() ) {
			if ( !arrayFind( arguments.before, f ) ) {
				try { fileDelete( openDir() & "/" & f ); } catch ( any e ) {}
			}
		}
	}

	private numeric function countSpoolerEOF() localmode = true {
		var total   = 0;
		var logsDir = expandPath( "{lucee-config}/logs" );
		if ( !directoryExists( logsDir ) ) return 0;
		for ( var file in directoryList( logsDir, false, "path", "*.log" ) ) {
			try {
				total += arrayLen( reMatch( 'SpoolerEngineImpl","java\.io\.(EOFException|StreamCorruptedException)', fileRead( file ) ) );
			}
			catch ( any e ) {}
		}
		return total;
	}
}
