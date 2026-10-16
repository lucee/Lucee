/*
 * Regression test for LDEV-6455.
 *
 * A spooled cfmail (spoolEnable=true) is handed to the spooler, which persists the task to disk via
 * Java serialization. mail-extension 1.1.0.6 gave the task a non-serializable
 * java.nio.charset.Charset field, so persisting it threw
 *   java.io.NotSerializableException: sun.nio.cs.UTF_8
 * and the mail was silently dropped (see the forum thread for LDEV-6455).
 *
 * The existing mail tests (Mail.cfc / Mail2.cfc / _Mail.cfc) never caught this because they all send
 * with spoolEnable=false, i.e. synchronously, which never touches the spooler's serialization path.
 *
 * This test drives the spooled path with a self-contained GreenMail SMTP mock (same approach as
 * Mail2.cfc) and asserts BOTH:
 *   1. the spooled mail is actually delivered, and
 *   2. no spooler-persistence failure is logged - i.e. the spool task is serializable.
 *
 * Note: the core-side fix for LDEV-6455 (SpoolerEngineImpl.add) executes a task inline when it cannot
 * be persisted, so with a buggy extension the mail is still delivered via that fallback; assertion (2)
 * is what surfaces the extension regression.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" javaSettings='{
		"maven": [
			"com.icegreen:greenmail:2.1.7"
		]
	}' {

	import "com.icegreen.greenmail.util.ServerSetup";
	import "com.icegreen.greenmail.util.GreenMail";
	processingdirective pageencoding="UTF-8";

	variables.port = 30251;
	variables.from = "susi@sorglos.de";
	variables.to   = "geisse@peter.ch";

	function beforeAll() {
		if ( isNull( application.testSMTPSpool ) ) {
			application.testSMTPSpool = new GreenMail( new ServerSetup( variables.port, nullValue(), ServerSetup::PROTOCOL_SMTP ) );
			application.testSMTPSpool.start();
		}
		else {
			application.testSMTPSpool.purgeEmailFromAllMailboxes();
		}
	}

	function afterAll() {
		if ( !isNull( application.testSMTPSpool ) ) {
			application.testSMTPSpool.purgeEmailFromAllMailboxes();
			application.testSMTPSpool.stop();
		}
	}

	function run( testResults, testBox ) {
		describe( title = "cfmail spooled delivery (LDEV-6455)", body = function() {

			it( title = "a spooled mail is serialized without error and delivered", body = function( currentSpec ) {
				lock name = "test:mailspool" timeout = 60 {
					application.testSMTPSpool.purgeEmailFromAllMailboxes();

					var errorsBefore = countSpoolerPersistErrors();

					mail to = variables.to from = variables.from subject = "spooled mail"
							spoolEnable = true server = "localhost" port = variables.port {
						echo( "This is a spooled email!" );
					}

					// delivered inline (buggy extension, via the core fallback) or by the async spooler (fixed)
					var messages = waitForMessages( application.testSMTPSpool, 1, 20000 );

					// (1) delivery contract
					expect( arrayLen( messages ) ).toBe( 1, "spooled mail was not delivered" );
					expect( messages[ 1 ].getSubject().toString() ).toBe( "spooled mail" );

					// (2) regression: the spool task must serialize cleanly - no persistence failure logged
					var errorsAfter = countSpoolerPersistErrors();
					expect( errorsAfter ).toBe( errorsBefore,
						"the spool task failed to serialize (NotSerializableException) - "
						& "mail-extension holds a non-serializable field; see the spooler persistence error in the logs" );
				}
			});

		});
	}

	// counts occurrences of the spooler-persistence-failure signature across every log file, so the
	// assertion does not depend on whether the error is routed to remoteclient.log or application.log.
	private numeric function countSpoolerPersistErrors() localmode = true {
		var total   = 0;
		var logsDir = expandPath( "{lucee-config}/logs" );
		if ( !directoryExists( logsDir ) ) return 0;
		for ( var file in directoryList( logsDir, false, "path", "*.log" ) ) {
			try {
				var content = fileRead( file );
				total += arrayLen( reMatchNoCase( "unable to persist spooler task|NotSerializableException", content ) );
			}
			catch ( any e ) {} // a log being rotated/locked must not fail the test
		}
		return total;
	}

	private array function waitForMessages( smtp, required numeric count, numeric timeout = 20000 ) localmode = true {
		var start = getTickCount();
		var msgs  = arguments.smtp.getReceivedMessages();
		while ( arrayLen( msgs ) < arguments.count && ( getTickCount() - start ) < arguments.timeout ) {
			sleep( 250 );
			msgs = arguments.smtp.getReceivedMessages();
		}
		return msgs;
	}
}
