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
					port="#myMailSettings.POP.securePort#",
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
					port="#myMailSettings.POP.insecurePort#",
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
					port="#myMailSettings.IMAP.securePort#",
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
					port="#myMailSettings.IMAP.insecurePort#",
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
		var result={};

		if(isNull(server.system)){
			// for lucee 4.5
			server.system = structNew();
			currSystem = createObject("java", "java.lang.System");
			server.system.environment = currSystem.getenv();
			server.system.properties = currSystem.getproperties();
		}

		if(
			!isNull(server.system.environment.MAIL_USERNAME) &&
			!isNull(server.system.environment.MAIL_PASSWORD) &&
			!isNull(server.system.environment.POP_SERVER) &&
			!isNull(server.system.environment.POP_PORT_SECURE) &&
			!isNull(server.system.environment.POP_PORT_INSECURE) &&
			!isNull(server.system.environment.IMAP_SERVER) &&
			!isNull(server.system.environment.IMAP_PORT_SECURE) &&
			!isNull(server.system.environment.IMAP_PORT_INSECURE) &&
			!isNull(server.system.environment.SMTP_SERVER) &&
			!isNull(server.system.environment.SMTP_PORT_SECURE) &&
			!isNull(server.system.environment.SMTP_PORT_INSECURE)
		){
			// getting the credentials from the environment variables
			var result={"POP":{},"IMAP":{},"SMTP":{}};
			// Common settings
			result.username=server.system.environment.MAIL_USERNAME;
			result.password=server.system.environment.MAIL_PASSWORD;
			// POP related settings
			result.POP.server=server.system.environment.POP_SERVER;
			result.POP.securePort=server.system.environment.POP_PORT_SECURE;
			result.POP.insecurePort=server.system.environment.POP_PORT_INSECURE;
			// IMAP related settings
			result.IMAP.server=server.system.environment.IMAP_SERVER;
			result.IMAP.securePort=server.system.environment.IMAP_PORT_SECURE;
			result.IMAP.insecurePort=server.system.environment.IMAP_PORT_INSECURE;
			// SMTP related settings
			result.SMTP.server=server.system.environment.SMTP_SERVER;
			result.SMTP.securePort=server.system.environment.SMTP_PORT_SECURE;
			result.SMTP.insecurePort=server.system.environment.SMTP_PORT_INSECURE;
		}else if(
			!isNull(server.system.properties.MAIL_USERNAME) &&
			!isNull(server.system.properties.MAIL_PASSWORD) &&
			!isNull(server.system.properties.POP_SERVER) &&
			!isNull(server.system.properties.POP_PORT_SECURE) &&
			!isNull(server.system.properties.POP_PORT_INSECURE) &&
			!isNull(server.system.properties.IMAP_SERVER) &&
			!isNull(server.system.properties.IMAP_PORT_SECURE) &&
			!isNull(server.system.properties.IMAP_PORT_INSECURE) &&
			!isNull(server.system.properties.SMTP_SERVER) &&
			!isNull(server.system.properties.SMTP_PORT_SECURE) &&
			!isNull(server.system.properties.SMTP_PORT_INSECURE)
		){
			// getting the credentials from the system properties
			var result={"POP":{},"IMAP":{},"SMTP":{}};
			// Common settings
			result.username=server.system.properties.MAIL_USERNAME;
			result.password=server.system.properties.MAIL_PASSWORD;
			// POP related settings
			result.POP.server=server.system.properties.POP_SERVER;
			result.POP.securePort=server.system.properties.POP_PORT_SECURE;
			result.POP.insecurePort=server.system.properties.POP_PORT_INSECURE;
			// IMAP related settings
			result.IMAP.server=server.system.properties.IMAP_SERVER;
			result.IMAP.securePort=server.system.properties.IMAP_PORT_SECURE;
			result.IMAP.insecurePort=server.system.properties.IMAP_PORT_INSECURE;
			// SMTP related settings
			result.SMTP.server=server.system.properties.SMTP_SERVER;
			result.SMTP.securePort=server.system.properties.SMTP_PORT_SECURE;
			result.SMTP.insecurePort=server.system.properties.SMTP_PORT_INSECURE;
		}

		return result;
	}
}
