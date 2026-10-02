component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" javaSettings='{
		"maven": [
			"com.icegreen:greenmail:2.1.7"
		]
	}' {

	import "com.icegreen.greenmail.util.ServerSetup";
	import "com.icegreen.greenmail.util.GreenMail";

	variables.port = 30258;
	variables.host = "127.0.0.1";

	function beforeAll() {
		variables.smtp = new GreenMail( new ServerSetup( variables.port, nullValue(), ServerSetup::PROTOCOL_SMTP ) );
		variables.smtp.start();
	}

	function afterAll() {
		if ( !isNull( variables.smtp ) ) variables.smtp.stop();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6458 mail server verification and admin test mail", function() {

			it( title="verifyMailServer fails for an unreachable mail server", body=function( currentSpec ) {
				// nothing is listening on port 1, before the fix the verification always "succeeded"
				expect( function() {
					admin action="verifyMailServer" type="server" password=server.SERVERADMINPASSWORD
						hostname=variables.host port="1" mailusername="" mailpassword="";
				}).toThrow();
			});

			it( title="verifyMailServer succeeds for a running mail server", body=function( currentSpec ) {
				admin action="verifyMailServer" type="server" password=server.SERVERADMINPASSWORD
					hostname=variables.host port=variables.port mailusername="" mailpassword="";
			});

			it( title="admin 'Send test mail' sends the mail", skip=noAdmin(), body=function( currentSpec ) {
				variables.smtp.purgeEmailFromAllMailboxes();
				admin action="updateMailServer" type="server" password=server.SERVERADMINPASSWORD
					hostname=variables.host port=variables.port dbusername="" dbpassword="" id="new"
					life=createTimeSpan( 0, 0, 1, 0 ) idle=createTimeSpan( 0, 0, 0, 10 );
				try {
					var adminRoot = getAdminRoot();
					var login = _internalRequest(
						template: adminRoot & "index.cfm",
						forms: { login_passwordserver: server.SERVERADMINPASSWORD, lang: "en", rememberMe: "s", submit: "submit" }
					);
					expect( login.status ).toBe( 200 );
					var cookies = { cfid: login.session.cfid, cftoken: login.session.cftoken };

					admin action="getMailServers" type="server" password=server.SERVERADMINPASSWORD returnVariable="local.ms";
					var row = 0;
					loop query=ms {
						if ( ms.hostname == variables.host && ms.port == variables.port ) row = ms.currentrow;
					}
					expect( row ).toBeGT( 0 );

					var result = _internalRequest(
						template: adminRoot & "index.cfm",
						urls: { action: "services.mail", row: row },
						forms: { mainAction: "Send test mail", toMail: "to@lucee.org", fromMail: "from@lucee.org" },
						cookies: cookies
					);
					expect( result.status ).toBe( 200 );
					expect( result.fileContent ).toInclude( "Test mail has been sent successfully" );

					var messages = variables.smtp.getReceivedMessages();
					expect( len( messages ) ).toBe( 1 );
					expect( messages[ 1 ].getSubject() ).toBe( "Test email from Lucee" );
				}
				finally {
					admin action="removeMailServer" type="server" password=server.SERVERADMINPASSWORD
						hostname=variables.host username="";
				}
			});

		});
	}

	// the CI build maps the admin source to /admin/ (see AdminPages.cfc), a plain jar serves it from /lucee/admin/ (archive),
	// light builds (e.g. script-runner in the extension CIs) have no admin at all, returns "" then
	private string function getAdminRoot() {
		var adminRoot = server.system.environment.LUCEE_TEST_ADMIN_PATH ?: "";
		if ( len( adminRoot ) ) return adminRoot;
		for ( adminRoot in [ "/admin/", "/lucee/admin/" ] ) {
			if ( !isNull( getPageContext().getRelativePageSourceExisting( adminRoot & "index.cfm" ) ) ) return adminRoot;
		}
		return "";
	}

	private boolean function noAdmin() {
		return !len( getAdminRoot() );
	}
}
