<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="pop"{

	function beforeAll() {
		variables.uri = createURI("LDEV3548"); 	
		variables.creds = getCredentials();

		systemOutput("creds: #serializeJSON(variables.creds)#",1,1);
		systemOutput("notHasServices: #notHasServices()#",1,1);

	}
		
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3548", function() {
			it( title="Checking CFPOP to retrieves the message/rfc822 body", skip="#notHasServices()#", body=function( currentSpec ) {
				var result = _internalRequest(
					template="#variables.uri#/sendMultipartMail.cfm"
				).filecontent.trim();

				expect(result).tobe("Done!!!"); // to check the mail has sended successfully
  
				pop action="getAll" name="local.inboxemails" server="#creds.pop.server#" password="#creds.pop.password#" port="#creds.pop.port#" secure="yes" username="#creds.pop.username#";
				
				multipartMessage = queryGetRow(inboxemails,queryRecordCount(inboxemails)); // assumes last inbox mail must sended by above the process

				expect(find("This is the body cfpop testcase LDEV-3548 to retrieve",multipartMessage.body)).tobeGT(0);
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
			pop : server.getTestService("pop")
		}
	}
		
	private function notHasServices() {
		return structCount(server.getTestService("smtp")) == 0 || structCount(server.getTestService("pop")) == 0;
	}
}
</cfscript>