/* LDEV-945: cfmail server="user:pass@host:port" fails when the username contains "@".
   Requires fake SMTP sink on 127.0.0.1:2526 (AUTH). Skipped when sink log absent. */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function run( testResults, testBox ) {
		describe( "LDEV-945 credentials in the server attribute", function() {

			it( title="username containing @ in server attribute", skip=noTlsSink(), body=function( currentSpec ) {
				var off = logSize();
				var err = "";
				try {
					mail server="user@domain.com:secretpw@127.0.0.1:2526" to="a@lucee.org" from="b@lucee.org"
							subject="LDEV945-#createUUID()#" spoolEnable=false { echo( "x" ); }
				}
				catch ( any e ) { err = e.message; }
				var log = logSince( off );
				systemOutput( "LDEV945 error: [" & err & "]", true );
				systemOutput( "LDEV945 smtp log: " & log, true );
				expect( err ).toBe( "" );
				expect( log ).toInclude( "user@domain.com" );
			});

			it( title="control: username without @ in server attribute", skip=noTlsSink(), body=function( currentSpec ) {
				var off = logSize();
				mail server="plainuser:secretpw@127.0.0.1:2526" to="a@lucee.org" from="b@lucee.org"
						subject="LDEV945-#createUUID()#" spoolEnable=false { echo( "x" ); }
				expect( logSince( off ) ).toInclude( "plainuser" );
			});

			it( title="workaround from the ticket: %40-encoded username", skip=noTlsSink(), body=function( currentSpec ) {
				var off = logSize();
				var err = "";
				try {
					mail server="user%40domain.com:pct40pw@127.0.0.1:2526" to="a@lucee.org" from="b@lucee.org"
							subject="LDEV945-#createUUID()#" spoolEnable=false { echo( "x" ); }
				}
				catch ( any e ) { err = e.message; }
				var log = logSince( off );
				systemOutput( "LDEV945 %40 error: [" & err & "] log: " & log, true );
				expect( err ).toBe( "" );
				expect( log ).toInclude( "user@domain.com" );
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
