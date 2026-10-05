/*
 * LDEV-3845: internationalised (UTF-8) email addresses.
 * - a non-ASCII local part needs SMTPUTF8 (RFC 6531): "MAIL FROM:<...> SMTPUTF8" and the address as UTF-8 in RCPT TO
 *   (Lucee sent it as raw Latin-1 bytes without SMTPUTF8, so it arrived garbled / was rejected as malformed)
 * - a non-ASCII domain can always be sent as IDNA A-label (punycode), no SMTPUTF8 needed
 * - plain ASCII mail must not change (no SMTPUTF8)
 * Needs a fake SMTP sink that advertises SMTPUTF8 and logs every client command (mail/_tools/smtp_sink.py),
 * MAIL_SINK_UTF8_PORT (default 2535) and MAIL_SINK_UTF8_LOG (its session.log). Skipped when the log is missing.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	variables.sinkLog  = server.system.environment.MAIL_SINK_UTF8_LOG ?: "/workspace/ldev-repro/mail/_smtp/utf8/session.log";
	variables.sinkPort = server.system.environment.MAIL_SINK_UTF8_PORT ?: 2535;

	function run( testResults, testBox ) {
		describe( "LDEV-3845 UTF-8 email addresses", function() {

			it( title="a UTF-8 local part is sent with SMTPUTF8 and as UTF-8", skip=noSink(), body=function( currentSpec ) {
				var r = sendAndLog( "ëmâil@somecompany.com" );
				expect( r.err ).toBe( "" );
				expect( r.log ).toInclude( "RCPT TO:<ëmâil@somecompany.com>", "RCPT TO is not the UTF-8 address" );
				expect( reFind( "MAIL FROM:<sender@lucee\.org>[^\n]*SMTPUTF8", r.log ) ).toBeGT( 0, "MAIL FROM has no SMTPUTF8 parameter" );
			});

			it( title="a non-ASCII domain is sent as IDNA A-label (punycode)", skip=noSink(), body=function( currentSpec ) {
				var r = sendAndLog( "info@bücher.example" );
				expect( r.err ).toBe( "" );
				expect( r.log ).toInclude( "RCPT TO:<info@xn--bcher-kva.example>", "domain not converted to its A-label" );
			});

			it( title="control: an ASCII-only mail does not use SMTPUTF8", skip=noSink(), body=function( currentSpec ) {
				var r = sendAndLog( "receiver@lucee.org" );
				expect( r.err ).toBe( "" );
				expect( r.log ).toInclude( "RCPT TO:<receiver@lucee.org>" );
				expect( r.log ).notToInclude( "SMTPUTF8" );
			});
		});
	}

	private boolean function noSink() {
		return !fileExists( variables.sinkLog );
	}

	private struct function sendAndLog( required string to ) {
		var off = len( fileRead( variables.sinkLog, "utf-8" ) );
		var err = "";
		try {
			mail to=arguments.to from="sender@lucee.org" subject="LDEV3845-#createUUID()#" charset="utf-8"
					server="127.0.0.1" port=variables.sinkPort spoolEnable=false { echo( "LDEV-3845" ); }
		}
		catch ( any e ) { err = e.message; }
		sleep( 300 );
		var log = mid( fileRead( variables.sinkLog, "utf-8" ), off + 1, 100000 );
		systemOutput( "LDEV3845 to=[#arguments.to#] err=[" & err & "] log=" & log, true );
		return { err: err, log: log };
	}
}
