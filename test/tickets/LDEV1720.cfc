/*
 * LDEV-1720: under high volume, spooled cfmail got stuck in the task queue
 * (NativeException "smtp" from Session.getService, i.e. a NoSuchProviderException).
 *
 * Sends a burst of spooled and sync mails from parallel threads (plus one spooled mail to a dead port)
 * to a self-contained GreenMail SMTP mock (same approach as MailSpool.cfc) and checks that every mail
 * is delivered and nothing is left in the spooler task queue.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" javaSettings='{
		"maven": [
			"com.icegreen:greenmail:2.1.7"
		]
	}' {

	import "com.icegreen.greenmail.util.ServerSetup";
	import "com.icegreen.greenmail.util.GreenMail";

	variables.port = 30252;
	variables.deadPort = 30259; // nothing listens here

	function beforeAll() {
		if ( isNull( application.testSMTPLDEV1720 ) ) {
			application.testSMTPLDEV1720 = new GreenMail( new ServerSetup( variables.port, nullValue(), ServerSetup::PROTOCOL_SMTP ) );
			application.testSMTPLDEV1720.start();
		}
		application.testSMTPLDEV1720.purgeEmailFromAllMailboxes();
	}

	function afterAll() {
		if ( !isNull( application.testSMTPLDEV1720 ) ) {
			application.testSMTPLDEV1720.purgeEmailFromAllMailboxes();
			application.testSMTPLDEV1720.stop();
			structDelete( application, "testSMTPLDEV1720" );
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1720 cfmail under load", function() {

			it( title="200 spooled mails sent from 10 parallel threads are all delivered and leave the queue", body=function( currentSpec ) {
				var prefix = "LDEV1720-spool-" & left( createUUID(), 8 ) & "-";
				burst( prefix, 10, 20, true );
				var got = waitForCount( prefix, 200, 120000 );
				expect( got ).toBe( 200, "not all spooled mails were delivered" );
				expect( tasksWithPrefix( prefix ).recordCount ).toBe( 0, "spooled mails left in the task queue" );
			});

			it( title="50 sync mails sent from 10 parallel threads are all delivered", body=function( currentSpec ) {
				var prefix = "LDEV1720-sync-" & left( createUUID(), 8 ) & "-";
				burst( prefix, 10, 5, false );
				var got = waitForCount( prefix, 50, 30000 );
				expect( got ).toBe( 50, "not all sync mails were delivered" );
			});

			it( title="one failing spooled mail does not block the others", body=function( currentSpec ) {
				var prefix = "LDEV1720-mixed-" & left( createUUID(), 8 ) & "-";
				var badSubject = prefix & "bad";
				try {
					mail to="r@lucee.org" from="s@lucee.org" subject=badSubject server="localhost" port=variables.deadPort spoolEnable=true {
						echo( "bad" );
					}
					burst( prefix, 5, 10, true );
					var got = waitForCount( prefix, 50, 90000 );
					expect( got ).toBe( 50, "a failing spooled mail blocked other spooled mails" );
					// only the failing mail may stay in the queue
					var left = tasksWithPrefix( prefix );
					expect( valueList( left.name ) ).toBe( left.recordCount ? badSubject : "" );
				}
				finally {
					removeTasks( tasksWithPrefix( prefix ) );
				}
			});
		});
	}

	private void function burst( required string prefix, required numeric threads, required numeric perThread, required boolean spool ) {
		var names = [];
		for ( var t = 1; t <= arguments.threads; t++ ) {
			var tn = "ldev1720_" & replace( createUUID(), "-", "", "all" );
			arrayAppend( names, tn );
			thread name=tn action="run" prefix=arguments.prefix t=t perThread=arguments.perThread spool=arguments.spool port=variables.port {
				for ( var i = 1; i <= attributes.perThread; i++ ) {
					mail to="r@lucee.org" from="s@lucee.org" subject="#attributes.prefix##attributes.t#-#i#"
							server="localhost" port=attributes.port spoolEnable=attributes.spool {
						echo( "LDEV-1720 #attributes.t#-#i#" );
					}
				}
			}
		}
		thread action="join" name=arrayToList( names ) timeout=60000;
		var errors = [];
		for ( var tn in names ) {
			if ( structKeyExists( cfthread[ tn ], "error" ) ) arrayAppend( errors, cfthread[ tn ].error.message );
		}
		expect( arrayToList( errors, " | " ) ).toBe( "", "cfmail threw inside a thread" );
	}

	private numeric function waitForCount( required string prefix, required numeric expected, numeric timeout=60000 ) {
		var start = getTickCount();
		while ( true ) {
			var n = 0;
			for ( var msg in application.testSMTPLDEV1720.getReceivedMessages() ) {
				if ( left( msg.getSubject() ?: "", len( arguments.prefix ) ) == arguments.prefix ) n++;
			}
			if ( n >= arguments.expected || ( getTickCount() - start ) > arguments.timeout ) return n;
			sleep( 250 );
		}
	}

	private query function tasksWithPrefix( required string prefix ) {
		admin action="getSpoolerTasks" type="server" password=server.SERVERADMINPASSWORD
				startrow="1" maxrow="10000" returnVariable="local.tasks";
		var p = arguments.prefix;
		return queryFilter( local.tasks, function( row ) { return left( row.name, len( p ) ) == p; } );
	}

	private void function removeTasks( required query q ) {
		for ( var row in arguments.q ) {
			admin action="removeSpoolerTask" type="server" password=server.SERVERADMINPASSWORD id=row.id;
		}
	}

}
