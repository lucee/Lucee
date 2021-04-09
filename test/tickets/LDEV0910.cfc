component extends="org.lucee.cfml.test.LuceeTestCase"{
	variables.isSupported=false;
	variables.myMailSettings=getCredentials();
	if(!structIsEmpty(myMailSettings))
		variables.isSupported=true;

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-910", skip=isNotSupported(!variables.isSupported), body=function() {
			it(title="checking cfpop tag with secure access", body = function( currentSpec ) {
				cfpop(
					action="getAll",
					username="#myMailSettings.Username#",
					password="#myMailSettings.Password#",
					server="#myMailSettings.POP.server#",
					port="#myMailSettings.POP.port_secure#",
					secure="true",
					name="local.result",
					maxrows = "10"
				);
				expect(result).toBeTypeOf("query");
				expect(result.columnList()).toBe("date,from,messagenumber,messageid,replyto,subject,cc,to,size,header,uid,body,textBody,HTMLBody,attachments,attachmentfiles,cids");
			});

			it(title="checking cfpop tag with insecure access", body = function( currentSpec ) {
				cfpop(
					action="getAll",
					username="#myMailSettings.Username#",
					password="#myMailSettings.Password#",
					server="#myMailSettings.POP.server#",
					port="#myMailSettings.POP.port_insecure#",
					secure="false",
					name="local.result",
					maxrows = "10"
				);
				expect(result).toBeTypeOf("query");
				expect(result.columnList()).toBe("date,from,messagenumber,messageid,replyto,subject,cc,to,size,header,uid,body,textBody,HTMLBody,attachments,attachmentfiles,cids");
			});

			it(title="checking cfimap tag with secure access", body = function( currentSpec ) {
				cfimap(
					action="getAll",
					username="#myMailSettings.Username#",
					password="#myMailSettings.Password#",
					server="#myMailSettings.IMAP.server#",
					port="#myMailSettings.IMAP.Port_secure#",
					secure="true",
					name="local.result",
					maxrows = "10"
				);
				expect(result).toBeTypeOf("query");
				expect(result.columnList()).toBe("date,from,messagenumber,messageid,replyto,subject,cc,to,size,header,uid,body,textBody,HTMLBody,attachments,attachmentfiles,cids");
			});

			it(title="checking cfimap tag with insecure access", body = function( currentSpec ) {
				cfimap(
					action="getAll",
					username="#myMailSettings.Username#",
					password="#myMailSettings.Password#",
					server="#myMailSettings.IMAP.server#",
					port="#myMailSettings.IMAP.port_insecure#",
					secure="false",
					name="local.result",
					maxrows = "10"
				);
				expect(result).toBeTypeOf("query");
				expect(result.columnList()).toBe("date,from,messagenumber,messageid,replyto,subject,cc,to,size,header,uid,body,textBody,HTMLBody,attachments,attachmentfiles,cids");
			});
		});
	}

	// private functions
	private boolean function isNotSupported( required boolean s1 ) {
		return arguments.s1;
	}

	private struct function getCredentials(){
		var result = server.getTestService("mail");
		if ( structCount(result) eq 0)
			return result;
		return {}; // TODO fix failing tests on travis https://luceeserver.atlassian.net/browse/LDEV-3431
		result.imap =  server.getTestService("imap");
		result.pop =  server.getTestService("pop");
		result.smtp = server.getTestService("smtp");
		return result;
	}
}
