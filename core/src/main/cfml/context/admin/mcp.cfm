<cfsetting showdebugoutput="false">
<cfscript>
	mcpDir = getDirectoryFromPath(getCurrentTemplatePath()) & "mcp/";
	if (!structKeyExists(application, "configMcpServer")) {
		application.configMcpServer = createObject("component", mcpDir & "ConfigMCPServer.cfc").init(mcpDir);
	}

	application.configMcpServer.handle();
</cfscript>
