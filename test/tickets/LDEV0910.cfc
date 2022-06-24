component extends="org.lucee.cfml.test.LuceeTestCase" labels="pop,imap" {
	variables.isSupported = false;
	variables.creds = getCredentials();
	systemOutput(creds, true);
	if( !structIsEmpty(creds.pop) && !structIsEmpty(creds.imap) )
		variables.isSupported=true;

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-910", skip=isNotSupported(!variables.isSupported), body=function() {
			it(title="checking cfpop tag with secure access", body = function( currentSpec ) {
				cfpop(
					action="getAll",
					username="#creds.pop.username#",
					password="#creds.pop.password#",
					server="#creds.POP.server#",
					port="#creds.POP.port_secure#",
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
					username="#creds.POP.username#",
					password="#creds.POP.password#",
					server="#creds.POP.server#",
					port="#creds.POP.port_insecure#",
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
					username="#creds.IMAP.username#",
					password="#creds.IMAP.password#",
					server="#creds.IMAP.server#",
					port="#creds.IMAP.Port_secure#",
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
					username="#creds.IMAP.username#",
					password="#creds.IMAP.password#",
					server="#creds.IMAP.server#",
					port="#creds.IMAP.port_insecure#",
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
		var result = {
			imap: server.getTestService("imap"),
			pop: server.getTestService("pop")
		}
		return result;
	}
}
