<cfsetting showdebugoutput="false">
<cfscript>
	if (!structKeyExists(application, "configMcpServer")) {
		application.configMcpServer = new lucee.admin.mcp.ConfigMCPServer();
	}

	application.configMcpServer.handle();
</cfscript>
