/* LDEV-614: cfmailparam file="ram://..." threw a NullPointerException.
   Needs a fake SMTP sink on 127.0.0.1:2525 that stores each message as an .eml file in a directory.
   Set MAIL_SINK_DIR to that directory; the specs are skipped when it is not set, so CI stays green. */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	variables.sinkDir = server.system.environment.MAIL_SINK_DIR ?: "";

	function run( testResults, testBox ) {
		describe( "LDEV-614 cfmailparam with a ram:// resource", function() {

			it( title="sync mail with a ram:// attachment is delivered with the attachment", skip=noSink(), body=function( currentSpec ) {
				var subject = "LDEV614-sync-" & createUUID();
				var f = "ram://LDEV614-" & createUUID() & ".txt";
				fileWrite( f, "LDEV-614 attachment content" );
				mail to="me@lucee.org" from="me@lucee.org" subject=subject server="127.0.0.1" port=2525 spoolEnable=false {
					mailparam file=f type="text/plain";
					echo( "body" );
				}
				var eml = waitForMail( subject );
				expect( len( eml ) ).toBeGT( 0, "mail not delivered" );
				expect( eml ).toInclude( listLast( f, "/" ) );
			});

			it( title="spooled mail with a ram:// attachment is delivered with the attachment", skip=noSink(), body=function( currentSpec ) {
				var subject = "LDEV614-spool-" & createUUID();
				var f = "ram://LDEV614-" & createUUID() & ".txt";
				fileWrite( f, "LDEV-614 spooled attachment" );
				mail to="me@lucee.org" from="me@lucee.org" subject=subject server="127.0.0.1" port=2525 spoolEnable=true {
					mailparam file=f type="text/plain";
					echo( "body" );
				}
				var eml = waitForMail( subject, 45 );
				expect( len( eml ) ).toBeGT( 0, "spooled mail not delivered within 45s" );
				expect( eml ).toInclude( listLast( f, "/" ) );
			});
		});
	}

	private boolean function noSink() {
		return !len( variables.sinkDir ) || !directoryExists( variables.sinkDir );
	}

	private string function waitForMail( required string subject, numeric seconds=10 ) {
		var end = getTickCount() + arguments.seconds * 1000;
		while ( getTickCount() < end ) {
			for ( var f in directoryList( variables.sinkDir, false, "path", "*.eml" ) ) {
				var c = fileRead( f, "utf-8" );
				if ( find( arguments.subject, c ) ) return c;
			}
			sleep( 250 );
		}
		return "";
	}

}
