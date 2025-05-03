<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="imap" {

	function beforeAll() {
		variables.uri = createURI("LDEV4147");
		variables.creds = getCredentials();

		if (notHasServices()) return;

		variables.username = "luceeldev4147imap@localhost";
		variables.sendingMails = _internalRequest(
			template="#variables.uri#/sendMails.cfm",
			forms = {username: variables.username}
		).filecontent;

		sleep(1000);

		variables.initialInboxMails = getInboxMails();
		variables.initialInboxCount = initialInboxMails.recordCount;
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4147", function() {

			beforeEach(function( currentSpec ){
				expect(variables.sendingMails).tobe("Done!!!"); // to check the mails has sended successfully
			});

			it( title="cfimap with maxRows attribute", skip="#notHasServices()#", body=function( currentSpec ) {
				var inboxmails = getInboxMails(maxRows=2);
				//expect( inboxmails.SEEN[1] ).toBeFalse();
				//expect( inboxmails.ANSWERED[2] ).toBeFalse();
				expect( inboxmails.recordCount ).tobe( 2 );

				//var inboxmails = getInboxMails(maxRows=2);
				//expect( inboxmails.SEEN[1] ).toBeTrue();
				//expect( inboxmails.SEEN[2] ).toBeTrue();
			});

			it( title="cfimap with maxRows and start rows attributes", skip="#notHasServices()#", body=function( currentSpec ) {
				var inboxmails = getInboxMails(maxRows=2,startRow=3);
				expect(inboxmails.recordCount).tobe(2);
				expect(inboxmails.messageNumber[1]).tobe(3);
				expect(inboxmails.messageNumber[2]).tobe(4);

				//expect( inboxmails.SEEN[1] ).toBeFalse();
				//expect( inboxmails.ANSWERED[2] ).toBeFalse();
			});

			it( title="cfimap delete mails using uids", skip="#notHasServices()#", body=function( currentSpec ) {
				var uids = queryColumnData(variables.initialInboxMails, "uid");

				imap action="delete"
					uid = "#uids[1]#,#uids[2]#,invalidUIDshouldIgnore"
					server="#creds.imap.SERVER#"
					password="#creds.imap.PASSWORD#"
					port="#creds.imap.PORT_INSECURE#"
					secure="no"
					username="#variables.username#";

				var result = getInboxMails(uid = "#uids[1]#,#uids[2]#");

				expect(result.recordCount).tobe(0);
			});

			it( title="cfimap delete mails using message numbers", skip="#notHasServices()#", body=function( currentSpec ) {

				var InboxCount = getInboxMails().recordCount;

				imap action="delete"
					messageNumber = "1,2,3,invalidUIDShouldIgnore,10000"
					server="#creds.imap.SERVER#"
					password="#creds.imap.PASSWORD#"
					port="#creds.imap.PORT_INSECURE#"
					secure="no"
					username="#variables.username#";

				expect(getInboxMails().recordCount).tobe(InboxCount - 3);
			});


			it( title="cfpop delete mails using message numbers", skip="#notHasServices()#", body=function( currentSpec ) {

				var InboxCount = getInboxMails().recordCount;

				pop action="delete"
					messageNumber = "1,2,3,invalidUIDShouldIgnore,10000"
					server="#creds.pop.SERVER#"
					password="#creds.pop.PASSWORD#"
					port="#creds.pop.PORT_INSECURE#"
					secure="no"
					username="#variables.username#";

				expect(getInboxMails().recordCount).tobe(InboxCount - 3);
			});

			it( title="cfpop delete mails using uids", skip="#notHasServices()#", body=function( currentSpec ) {
				var uids = queryColumnData(variables.initialInboxMails, "uid");

				pop action="delete"
					uid = "#uids[initialInboxCount]#,#uids[initialInboxCount-1]#,invalidUIDshouldIgnore"
					server="#creds.pop.SERVER#"
					password="#creds.pop.PASSWORD#"
					port="#creds.pop.PORT_INSECURE#"
					secure="no"
					username="#variables.username#";

				var result = getInboxMails(uid = "#uids[initialInboxCount]#,#uids[initialInboxCount-1]#");

				expect(result.recordCount).tobe(0);
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function getCredentials() {
		return {
			smtp : server.getTestService("smtp"),
			imap : server.getTestService("imap"),
			pop : server.getTestService("pop")
		}
	}

	private function notHasServices() {
		return structCount(server.getTestService("smtp")) == 0 || structCount(server.getTestService("imap")) == 0 || structCount(server.getTestService("pop")) == 0;
	}

	private query function getInboxMails(String uid = "", String messageNumber="",number maxRows=0, number startRow=0) {
		var mails = "";
		var attrs = {
			action="getAll",
			server="#creds.imap.SERVER#",
			username="#variables.username#",
			password="#creds.imap.PASSWORD#",
			port="#creds.imap.PORT_INSECURE#",
			secure="no",
			name="local.mails"
		}

		if (arguments.uid != "") attrs.uid = arguments.uid;
		if (arguments.messageNumber != "") attrs.messageNumber = arguments.messageNumber;
		if (arguments.maxRows != 0) attrs.maxRows = arguments.maxRows;
		if (arguments.startRow != 0) attrs.startRow = arguments.startRow;

		imap attributeCollection = "#attrs#";

		return mails;
	}


	function afterAll() {

		structDelete(server, "mailsErrorMessage");

		if (!notHasServices()) { // delete all the inbox mails
			imap action="delete"
				server="#creds.imap.SERVER#"
				password="#creds.imap.PASSWORD#"
				port="#creds.imap.PORT_INSECURE#"
				secure="no"
				username="#variables.username#";
		}
	}

}
</cfscript>