/*
 * LDEV-3092: the mail spooler keeps retrying a mail whose attachment file is gone. The file will not come back,
 * so the task should be closed after the attempt instead of being rescheduled.
 * Also checks that a failed attempt is counted (tries), which the retry plan (1m, 5m, 1h, 2x 24h) depends on.
 * Self-contained: the failing mails go to the closed port 1. The delivery control needs the fake SMTP sink on
 * 127.0.0.1:2525 (mail/_tools/smtp_sink.py, MAIL_SINK_DIR) and is skipped without it.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	variables.sinkDir = server.system.environment.MAIL_SINK_DIR ?: "/workspace/ldev-repro/mail/_smtp/plain";

	function beforeAll() {
		variables.engine = getPageContext().getConfig().getSpoolerEngine();
		variables.rcDir = getPageContext().getConfig().getRemoteClientDirectory();
		variables.tmp = getTempDirectory() & "LDEV3092-" & createUUID() & "/";
		directoryCreate( variables.tmp );
	}

	function afterAll() {
		if ( directoryExists( variables.tmp ) ) directoryDelete( variables.tmp, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3092 spooled mail with a missing attachment", function() {

			it( title="a spooled mail whose attachment file is gone is closed, not rescheduled", body=function( currentSpec ) {
				var subject = "LDEV3092-missing-" & createUUID();
				var file = variables.tmp & "gone-" & createUUID() & ".txt";
				fileWrite( file, "LDEV-3092" );
				mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject
						server="127.0.0.1" port=1 spoolEnable=true sendTime=dateAdd( "s", 3, now() ) {
					mailparam file=file;
					echo( "LDEV-3092 missing attachment" );
				}
				fileDelete( file ); // e.g. a temp file removed by the app before the spooler sends the mail

				var res = waitForAttempt( subject, 30 );
				systemOutput( "LDEV3092 missing: " & serializeJSON( res ).left( 800 ), true );
				cleanup( res );
				expect( res.executed ).toBeTrue( "spooled mail was not attempted within 30s" );
				expect( res.open ).toBeFalse( "mail with a missing attachment is still in the open queue and will be retried (next: #res.nextExecution#)" );
				expect( res.closed ).toBeTrue( "mail with a missing attachment was not moved to the closed tasks" );
			});

			it( title="a failed attempt is counted (tries) and the mail is rescheduled", body=function( currentSpec ) {
				var subject = "LDEV3092-tries-" & createUUID();
				var file = variables.tmp & "here-" & createUUID() & ".txt";
				fileWrite( file, "LDEV-3092" );
				mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject
						server="127.0.0.1" port=1 spoolEnable=true {
					mailparam file=file;
					echo( "LDEV-3092 closed port" );
				}
				var res = waitForAttempt( subject, 30 );
				systemOutput( "LDEV3092 tries: " & serializeJSON( res ).left( 800 ), true );
				cleanup( res );
				expect( res.executed ).toBeTrue( "spooled mail was not attempted within 30s" );
				expect( res.open ).toBeTrue( "a mail that failed because the server was down must stay in the queue for a retry" );
				expect( res.tries ).toBe( 1, "the failed attempt was not counted, so the retry plan never advances" );
			});

			it( title="control: a spooled mail with an existing attachment is delivered", skip=!directoryExists( variables.sinkDir ), body=function( currentSpec ) {
				var subject = "LDEV3092-ok-" & createUUID();
				var name = "here-" & createUUID() & ".txt";
				fileWrite( variables.tmp & name, "LDEV-3092" );
				mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject
						server="127.0.0.1" port=2525 spoolEnable=true {
					mailparam file=variables.tmp & name;
					echo( "LDEV-3092 delivered" );
				}
				var eml = waitForMail( subject, 30 );
				expect( len( eml ) ).toBeGT( 0, "spooled mail never reached the SMTP sink" );
				expect( eml ).toInclude( name );
			});
		});
	}

	// waits until the spooler has tried the task once (lastExecution set), then reports where it is
	private struct function waitForAttempt( required string subject, numeric seconds=30 ) {
		var s = arguments.subject;
		var res = { executed: false, open: false, closed: false, tries: -1, nextExecution: "", id: "", exceptions: "" };
		var end = getTickCount() + arguments.seconds * 1000;
		while ( getTickCount() < end ) {
			var o = queryFilter( variables.engine.getOpenTasksAsQuery( 1, 10000 ), function( row ) { return row.name == s; } );
			var c = queryFilter( variables.engine.getClosedTasksAsQuery( 1, 10000 ), function( row ) { return row.name == s; } );
			var q = c.recordCount ? c : o;
			if ( q.recordCount && year( q.lastExecution ) > 2000 ) {
				sleep( 500 ); // let the spooler finish writing the task back
				o = queryFilter( variables.engine.getOpenTasksAsQuery( 1, 10000 ), function( row ) { return row.name == s; } );
				c = queryFilter( variables.engine.getClosedTasksAsQuery( 1, 10000 ), function( row ) { return row.name == s; } );
				q = c.recordCount ? c : o;
				res.executed = true;
				res.open = o.recordCount > 0;
				res.closed = c.recordCount > 0;
				res.tries = q.tries[ 1 ];
				res.nextExecution = q.nextExecution[ 1 ];
				res.id = q.id[ 1 ];
				var ex = q.exceptions[ 1 ];
				res.exceptions = isArray( ex ) && arrayLen( ex ) ? ex[ arrayLen( ex ) ].message : "";
				return res;
			}
			sleep( 250 );
		}
		return res;
	}

	private void function cleanup( required struct res ) {
		if ( !len( res.id ) ) return;
		for ( var d in [ "open", "closed" ] ) {
			var f = variables.rcDir.getRealResource( d & "/" & res.id & ".tsk" ).getAbsolutePath();
			if ( fileExists( f ) ) fileDelete( f );
		}
	}

	private string function waitForMail( required string subject, numeric seconds=20 ) {
		var end = getTickCount() + arguments.seconds * 1000;
		while ( getTickCount() < end ) {
			for ( var f in directoryList( variables.sinkDir, false, "path", "*.eml" ) ) {
				var c = fileRead( f, "utf-8" );
				if ( find( arguments.subject, c ) ) return c;
			}
			sleep( 500 );
		}
		return "";
	}
}
