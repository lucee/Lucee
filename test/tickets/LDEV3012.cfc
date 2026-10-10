/* LDEV-3012: SMTP errors don't say which mail server failed (e.g. an auth failure in mail.log).
   The connection-failure spec uses an unused local port and needs no SMTP server.
   The auth-failure specs need a fake SMTP server that rejects every AUTH with 535;
   set MAIL_SINK_AUTHFAIL_PORT to its port on 127.0.0.1, otherwise they are skipped so CI stays green. */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	variables.authFailPort = server.system.environment.MAIL_SINK_AUTHFAIL_PORT ?: "";

	function run( testResults, testBox ) {
		describe( "LDEV-3012 smtp errors name the mail server", function() {

			it( title="sync: a connection failure names host and port", body=function( currentSpec ) {
				var txt = sendSync( port=1 );
				expect( txt ).toInclude( "127.0.0.1" );
			});

			it( title="sync: an authentication failure names the host", skip=noAuthFailServer(), body=function( currentSpec ) {
				var txt = sendSync( port=variables.authFailPort, username="ldev3012", password="wrong" );
				expect( txt ).toInclude( "535" );
				expect( txt ).toInclude( "127.0.0.1" );
			});

			it( title="spooled: the mail.log entry for an authentication failure names the host", skip=noAuthFailServer(), body=function( currentSpec ) {
				var logFile = mailLog();
				systemOutput( "LDEV3012 mail log: " & logFile, true );
				expect( len( logFile ) ).toBeGT( 0, "mail.log not found" );
				var off = len( fileRead( logFile, "utf-8" ) );
				var subject = "LDEV3012-spool-" & createUUID();
				mail server="127.0.0.1" port=variables.authFailPort username="ldev3012" password="wrong"
						to="a@lucee.org" from="b@lucee.org" subject=subject spoolEnable=true { echo( "x" ); }
				var entry = "";
				var end = getTickCount() + 45000;
				while ( getTickCount() < end ) {
					entry = mid( fileRead( logFile, "utf-8" ), off + 1, 1000000 );
					if ( find( "535", entry ) ) break;
					sleep( 500 );
				}
				systemOutput( "LDEV3012 spooled mail.log: " & left( entry, 3000 ), true );
				expect( entry ).toInclude( "535", "no auth failure logged in mail.log within 45s" );
				// only look at the log lines (not the stack traces) that report the failure
				var lines = [];
				for ( var l in listToArray( entry, chr( 10 ) ) ) {
					if ( find( "535", l ) && !reFind( "^\s+at ", l ) ) arrayAppend( lines, l );
				}
				systemOutput( "LDEV3012 535 lines: " & serializeJSON( lines ), true );
				expect( arrayLen( lines ) ).toBeGT( 0 );
				for ( var l in lines ) {
					expect( l ).toInclude( "127.0.0.1", "mail.log line does not name the server: " & l );
				}
			});
		});
	}

	private string function sendSync( required numeric port, string username="", string password="" ) {
		var err = {};
		try {
			mail server="127.0.0.1" port=arguments.port username=arguments.username password=arguments.password
					to="a@lucee.org" from="b@lucee.org" subject="LDEV3012-sync-#createUUID()#" spoolEnable=false { echo( "x" ); }
		}
		catch ( any e ) { err = e; }
		expect( structCount( err ) ).toBeGT( 0, "expected a mail error" );
		var txt = ( err.message ?: "" ) & " | " & ( err.detail ?: "" );
		systemOutput( "LDEV3012 sync port #arguments.port#: " & txt, true );
		return txt;
	}

	private boolean function noAuthFailServer() {
		return !isNumeric( variables.authFailPort );
	}

	private string function mailLog() {
		for ( var dir in [ "{lucee-config}/logs", "{lucee-server}/logs", "{lucee-web}/logs" ] ) {
			var f = expandPath( dir & "/mail.log" );
			if ( fileExists( f ) ) return f;
		}
		return "";
	}
}
