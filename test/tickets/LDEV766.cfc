/* LDEV-766: useTLS=true does not issue STARTTLS when no username is given (mail silently goes out in plain text).
   Requires a fake SMTP sink that advertises STARTTLS (see mail/_tools/smtp_sink.py).
   Set MAIL_SINK_TLS_LOG to the sink session.log path; skipped in CI when unset / missing. */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function run( testResults, testBox ) {
		describe( "LDEV-766 useTLS without username", function() {

			it( title="useTLS=true without username issues STARTTLS", skip=noTlsSink(), body=function( currentSpec ) {
				var off = logSize();
				var err = "";
				try {
					mail server="127.0.0.1" port=2526 useTLS=true to="a@lucee.org" from="b@lucee.org"
							subject="LDEV766-nouser-#createUUID()#" spoolEnable=false { echo( "x" ); }
				}
				catch ( any e ) { err = e.message; }
				var log = logSince( off );
				systemOutput( "LDEV766 no user: err=[" & err & "] log=" & log, true );
				expect( log ).toInclude( "C: STARTTLS", "client never tried STARTTLS although useTLS=true" );
				expect( log ).notToInclude( "DATA stored", "mail was sent in plain text although useTLS=true" );
			});

			it( title="control: useTLS=true with username issues STARTTLS", skip=noTlsSink(), body=function( currentSpec ) {
				var off = logSize();
				var err = "";
				try {
					mail server="127.0.0.1" port=2526 useTLS=true username="anyStringHere" password="x" to="a@lucee.org" from="b@lucee.org"
							subject="LDEV766-user-#createUUID()#" spoolEnable=false { echo( "x" ); }
				}
				catch ( any e ) { err = e.message; }
				var log = logSince( off );
				systemOutput( "LDEV766 with user: err=[" & err & "] log=" & log, true );
				expect( log ).toInclude( "C: STARTTLS" );
			});
		});
	}

	variables.tlsLog = server.system.environment.MAIL_SINK_TLS_LOG ?: "/workspace/ldev-repro/mail/_smtp/tls/session.log";

	private boolean function noTlsSink() {
		return !fileExists( variables.tlsLog );
	}

	private numeric function logSize() {
		return fileExists( variables.tlsLog ) ? len( fileRead( variables.tlsLog, "utf-8" ) ) : 0;
	}

	private string function logSince( required numeric offset ) {
		sleep( 300 );
		var c = fileExists( variables.tlsLog ) ? fileRead( variables.tlsLog, "utf-8" ) : "";
		return mid( c, arguments.offset + 1, len( c ) );
	}

}
