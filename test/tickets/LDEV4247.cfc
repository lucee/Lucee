<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="pop,imap" skip=true {

	function beforeAll() {
		variables.uri = createURI("LDEV4247"); 	
		variables.creds = getCredentials();
		
		if (notHasServices()) return;

		variables.username = "luceeldev4247@localhost";
		variables.sendingMails = _internalRequest(
			template="#variables.uri#/sendMails.cfm",
			forms = {username: variables.username}
		).filecontent;

		sleep(1000);

		variables.InitailInboxMails = getInboxMails();
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4247", function() {
			
			beforeEach(function( currentSpec ){
				expect(variables.sendingMails).tobe("Done!!!"); // to check the mail has been sent successfully
			});
			
			it( title="cfpop with delimiter attribute", skip="#notHasServices()#", body=function( currentSpec ) {
				var uids = queryColumnData(variables.InitailInboxMails, "uid");
				
				var result = _internalRequest(
					template="#variables.uri#/LDEV4247.cfm",
					forms = {
						username: variables.username,
						uids = "#uids[1]#|#uids[2]#$#uids[3]#|$#uids[4]#",
						scene = 1
					}
				).filecontent;

				expect(result.trim()).tobe(4);
			});

			it( title="cfpop with delimiter and without uid attribute should throw error", skip="#notHasServices()#", body=function( currentSpec ) {
				expect(
					() => {
						_internalRequest(
							template="#variables.uri#/LDEV4247.cfm",
							forms = {
								username: variables.username,
								scene = 2
							}
						);
					}
				).toThrow();
			});

			it( title="cfimap with delimiter attribute", skip="#notHasServices()#", body=function( currentSpec ) {
				var uids = queryColumnData(variables.InitailInboxMails, "uid");

				var result = _internalRequest(
					template="#variables.uri#/LDEV4247.cfm",
					forms = {
						username: variables.username,
						uids = "#uids[1]#|#uids[2]#$#uids[3]#|$#uids[4]#",
						scene = 3
					}
				).filecontent;

				expect(result.trim()).tobe(4);
			});

			it( title="cfimap with delimiter and without uid attribute should throw error", skip="#notHasServices()#", body=function( currentSpec ) {
				expect(
					() => {
						_internalRequest(
							template="#variables.uri#/LDEV4247.cfm",
							forms = {
								username: variables.username,
								scene = 4
							}
						);
					}
				).toThrow();
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

	private query function getInboxMails() {
		var mails = "";
		imap action="getAll",
			server="#creds.imap.SERVER#",
			username="#variables.username#",
			password="#creds.imap.PASSWORD#",
			port="#creds.imap.PORT_INSECURE#",
			secure="no",
			name="local.mails";

		return mails;
	}


	function afterAll() {

		structDelete(server, "mailsErrorMessage");

		if (!notHasServices()) { // delete all the inbox mails after the tests in the thread
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
