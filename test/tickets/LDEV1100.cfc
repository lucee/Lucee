/*
 * LDEV-1100: cfpop failed with 'Command unrecognized "USER"' against a server that needs SSL;
 * the reporter had to fall back to javax.mail with an SSL socket factory on port 995.
 * cfpop supports this directly with secure=true (LDEV-132).
 *
 * Uses a self-contained GreenMail mock (SMTP + POP3 + POP3S, same approach as MailSpool.cfc):
 * a mail is delivered via cfmail and then read with cfpop over plain POP3 (USER/PASS) and over POP3S.
 * GreenMail's POP3S uses a self-signed cert, so lucee.ssl.checkserveridentity=false is set during the secure specs.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" javaSettings='{
		"maven": [
			"com.icegreen:greenmail:2.1.7"
		]
	}' {

	variables.user = "ldev1100@localhost";
	variables.pass = "ldev1100pass";

	// a component UDF, not a closure: a closure's variables writes stay in the closure scope
	function startGreenMail() {
		variables.helper = new test._GreenMail();
		variables.greenMail = variables.helper.start( [ "smtp", "pop", "pops" ] );
		variables.helper.user( variables.greenMail, variables.user, variables.pass );

		variables.subject = "LDEV1100-" & createUUID();
		mail to=variables.user from="sender@localhost" subject=variables.subject server="localhost" port=variables.greenMail.ports.smtp spoolEnable=false {
			echo( "LDEV-1100 body" );
		}
		variables.helper.waitForIncoming( variables.greenMail.server, 1 );
	}

	function stopGreenMail() {
		variables.helper.stop( variables.greenMail );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-1100 cfpop login (USER/PASS)", function() {
			beforeEach( startGreenMail );
			afterEach( stopGreenMail );
			it( title="getHeaderOnly over plain POP3 logs in and lists the message", body=function( currentSpec ) {
					pop action="getHeaderOnly" server="localhost" port=variables.greenMail.ports.pop secure=false
							username=variables.user password=variables.pass name="local.q" timeout=20;
				expect( q.recordCount ).toBe( 1 );
				expect( q.subject ).toBe( variables.subject );
			});

			it( title="getHeaderOnly with secure=true (POP3S) logs in and lists the message", body=function( currentSpec ) {
				var q = secure( function() {
					pop action="getHeaderOnly" server="localhost" port=variables.greenMail.ports.pops secure=true
							username=variables.user password=variables.pass name="local.q" timeout=20;
					return q;
				} );
				expect( q.recordCount ).toBe( 1 );
				expect( q.subject ).toBe( variables.subject );
			});

			it( title="getAll with secure=true (POP3S) returns the body", body=function( currentSpec ) {
				var q = secure( function() {
					pop action="getAll" server="localhost" port=variables.greenMail.ports.pops secure=true
							username=variables.user password=variables.pass name="local.q" timeout=20;
					return q;
				} );
				expect( q.recordCount ).toBe( 1 );
				expect( trim( q.body ) ).toBe( "LDEV-1100 body" );
			});

			it( title="a wrong password gives an authentication error, not a protocol error", body=function( currentSpec ) {
				var err = "";
				try {
					pop action="getHeaderOnly" server="localhost" port=variables.greenMail.ports.pop secure=false
							username=variables.user password="wrong" name="local.q" timeout=20;
				}
				catch ( any e ) {
					err = e.message;
				}
				expect( err ).notToBe( "", "login with a wrong password did not fail" );
				expect( err ).notToInclude( "unrecognized" );
			});
		});
	}

	// GreenMail's POP3S has a self-signed certificate
	private any function secure( required function fn ) {
		var sys = createObject( "java", "java.lang.System" );
		var old = sys.getProperty( "lucee.ssl.checkserveridentity" );
		sys.setProperty( "lucee.ssl.checkserveridentity", "false" );
		try {
			return fn();
		}
		finally {
			if ( isNull( old ) ) sys.clearProperty( "lucee.ssl.checkserveridentity" );
			else sys.setProperty( "lucee.ssl.checkserveridentity", old );
		}
	}

}
