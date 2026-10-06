/*
 * LDEV-2967: useTLS=true failed with "Could not convert socket to TLS" /
 * "SSLHandshakeException: A potential protocol version downgrade attack" (e.g. Office 365).
 * Cause: Lucee set mail.smtp.ssl.protocols to all protocols the JDK *supports* (incl. SSLv2Hello),
 * so the client could not negotiate TLS 1.3 properly. Since LDEV-5893 Lucee uses the JDK's *enabled* protocols.
 *
 * Spec 1 runs everywhere: after a useTLS mail, the protocols Lucee set must not contain SSLv2Hello/SSLv3/TLSv1/TLSv1.1.
 * Spec 2 needs an SMTP server with real STARTTLS (TLS 1.2 + 1.3, any cert) on 127.0.0.1:MAIL_TLS_SINK_PORT
 * that writes a session log (with "TLS OK version=..." lines) to MAIL_TLS_SINK_LOG; it is skipped when not set.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	variables.tlsPort = server.system.environment.MAIL_TLS_SINK_PORT ?: "";
	variables.tlsLog = server.system.environment.MAIL_TLS_SINK_LOG ?: "";
	variables.deadPort = 30269; // nothing listens here

	function run( testResults, testBox ) {
		describe( "LDEV-2967 STARTTLS protocols", function() {

			it( title="useTLS only enables TLS protocols the JDK has enabled (no SSLv2Hello, SSLv3, TLSv1, TLSv1.1)", skip=hasOverride(), body=function( currentSpec ) {
				try {
					mail server="127.0.0.1" port=variables.deadPort useTLS=true username="ldev2967" password="x"
							to="a@lucee.org" from="b@lucee.org" subject="LDEV2967-props" spoolEnable=false timeout=5 {
						echo( "x" );
					}
				}
				catch ( any e ) {} // nobody listens, we only care about the protocols Lucee configured
				var protocols = createObject( "java", "java.lang.System" ).getProperty( "mail.smtp.ssl.protocols", "" );
				systemOutput( "LDEV2967 mail.smtp.ssl.protocols=[#protocols#]", true );
				expect( len( protocols ) ).toBeGT( 0, "Lucee did not set mail.smtp.ssl.protocols" );
				loop list="SSLv2Hello,SSLv3,TLSv1,TLSv1.1" item="local.bad" {
					expect( listFindNoCase( protocols, bad, " ," ) ).toBe( 0, "deprecated protocol [#bad#] enabled for STARTTLS: [#protocols#]" );
				}
			});

			it( title="useTLS=true sends via STARTTLS to a TLS 1.2/1.3 server and negotiates TLS 1.3", skip=noTlsSink(), body=function( currentSpec ) {
				var subject = "LDEV2967-" & createUUID();
				var sys = createObject( "java", "java.lang.System" );
				var oldTrust = sys.getProperty( "mail.smtp.ssl.trust" );
				sys.setProperty( "mail.smtp.ssl.trust", "127.0.0.1" ); // the test server uses a self-signed cert
				var off = logSize();
				var err = "";
				try {
					mail server="127.0.0.1" port=variables.tlsPort useTLS=true username="ldev2967" password="x"
							to="a@lucee.org" from="b@lucee.org" subject=subject spoolEnable=false {
						echo( "LDEV-2967" );
					}
				}
				catch ( any e ) {
					err = e.message & " " & ( e.detail ?: "" );
				}
				finally {
					if ( isNull( oldTrust ) ) sys.clearProperty( "mail.smtp.ssl.trust" );
					else sys.setProperty( "mail.smtp.ssl.trust", oldTrust );
				}
				var log = logSince( off );
				systemOutput( "LDEV2967 err=[#err#] log=#log#", true );
				expect( err ).toBe( "", "sending with useTLS failed" );
				expect( log ).toInclude( "TLS OK version=TLSv1.3" );
				expect( log ).toInclude( "tls=True" );
			});
		});
	}

	private boolean function hasOverride() {
		// a user/env override of mail.smtp.ssl.protocols is respected as is
		return len( server.system.environment[ "mail.smtp.ssl.protocols" ] ?: server.system.environment.MAIL_SMTP_SSL_PROTOCOLS ?: "" ) > 0;
	}

	private boolean function noTlsSink() {
		return !len( variables.tlsPort ) || !len( variables.tlsLog );
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
