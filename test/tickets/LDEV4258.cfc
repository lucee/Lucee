/* LDEV-4258: several cfmailpart with the same type - only the first part of a type is sent.
   Requires fake SMTP sink on 127.0.0.1:2525 (MAIL_SINK_DIR). Skipped when sink dir absent. */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	variables.sinkDir  = server.system.environment.MAIL_SINK_DIR ?: "/workspace/ldev-repro/mail/_smtp/plain";

	function run( testResults, testBox ) {
		describe( "LDEV-4258 multiple cfmailpart of the same type", function() {

			it( title="two text/plain parts are both sent", skip=noPlainSink(), body=function( currentSpec ) {
				var subject = "LDEV4258-a-" & createUUID();
				mail from="test@lucee.org" to="abc@lucee.org" subject=subject server="127.0.0.1" port=2525 spoolEnable=false {
					mailpart type="text/plain" { echo( "First mailpart" ); }
					mailpart type="text/plain" { echo( "Second mailpart" ); }
				}
				var eml = waitForMail( subject );
				expect( len( eml ) ).toBeGT( 0, "mail not delivered" );
				expect( eml ).toInclude( "First mailpart" );
				expect( eml ).toInclude( "Second mailpart" );
			});

			it( title="text + html + second text part: all three are sent", skip=noPlainSink(), body=function( currentSpec ) {
				var subject = "LDEV4258-b-" & createUUID();
				mail from="test@lucee.org" to="abc@lucee.org" subject=subject server="127.0.0.1" port=2525 spoolEnable=false {
					mailpart type="text/plain" { echo( "Part one plain" ); }
					mailpart type="text/html" { echo( "<b>Part two html</b>" ); }
					mailpart type="text/plain" { echo( "Part three plain" ); }
				}
				var eml = waitForMail( subject );
				expect( len( eml ) ).toBeGT( 0, "mail not delivered" );
				expect( eml ).toInclude( "Part one plain" );
				expect( eml ).toInclude( "Part two html" );
				expect( eml ).toInclude( "Part three plain" );
			});
		});
	}

	private boolean function noPlainSink() {
		return !directoryExists( variables.sinkDir );
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
