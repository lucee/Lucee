component extends="Tool" {

	variables.name = "config_update";
	variables.description = "Update a Lucee configuration section via Administrator.cfc (requires write access).";
	variables.writeTool = true;
	variables.blockedAreas = "restart,changeversionto,runupdate,password";

	public function init(string accessLevel="none", any util=nullValue()) {
		super.init(arguments.accessLevel, arguments.util);
		variables.inputSchema = buildInputSchema({
			"area": {
				"type": "string",
				"description": "Configuration section to update"
			},
			"settings": {
				"type": "object",
				"description": "Settings to apply for the selected area"
			},
			"name": {
				"type": "string",
				"description": "Optional item name for remove/update operations"
			}
		}, ["password", "area", "settings"]);
		return this;
	}

	public any function exec(required any administrator, required struct args) {
		var area = lCase(trim(arguments.args.area ?: ""));
		var settings = arguments.args.settings ?: {};
		var name = trim(arguments.args.name ?: "");

		if (!len(area)) {
			throw(message="Missing area", type="mcp.tool");
		}
		if (!isStruct(settings)) {
			throw(message="settings must be an object", type="mcp.tool");
		}
		if (listFindNoCase(variables.blockedAreas, area)) {
			throw(message="Area [#area#] cannot be changed via MCP", type="mcp.tool");
		}

		if (area == "ai" && structKeyExists(settings, "verify") && settings.verify) {
			return serializeResult(arguments.administrator.verifyAIConnection(name=requiredName(name, "AI connection")));
		}

		updateArea(arguments.administrator, area, settings, name);

		return serializeResult({
			"ok": true,
			"area": area
		});
	}

	private void function updateArea(required any administrator, required string area, required struct settings, string name="") {
		switch (arguments.area) {
			case "regional":
				arguments.administrator.updateRegional(
					timezone=arg(arguments.settings, "timezone"),
					locale=arg(arguments.settings, "locale")
				);
				break;
			case "charset":
				arguments.administrator.updateCharset(
					resourceCharset=arg(arguments.settings, "resourceCharset"),
					templateCharset=arg(arguments.settings, "templateCharset"),
					webCharset=arg(arguments.settings, "webCharset")
				);
				break;
			case "scope":
				arguments.administrator.updateScope(argumentCollection=arguments.settings);
				break;
			case "application":
				arguments.administrator.updateApplicationSetting(
					requestTimeout=arg(arguments.settings, "requestTimeout"),
					scriptProtect=arg(arguments.settings, "scriptProtect"),
					allowURLRequestTimeout=arg(arguments.settings, "allowURLRequestTimeout")
				);
				break;
			case "queue":
				arguments.administrator.updateQueueSetting(
					requestQueueMax=arg(arguments.settings, "requestQueueMax"),
					requestQueueTimeout=arg(arguments.settings, "requestQueueTimeout"),
					requestQueueEnable=arg(arguments.settings, "requestQueueEnable")
				);
				break;
			case "output":
				arguments.administrator.updateOutputSetting(
					cfmlWriter=arg(arguments.settings, "cfmlWriter"),
					suppressContent=arg(arguments.settings, "suppressContent"),
					allowCompression=arg(arguments.settings, "allowCompression"),
					bufferOutput=arg(arguments.settings, "bufferOutput")
				);
				break;
			case "mappings":
				if (structKeyExists(arguments.settings, "remove") && arguments.settings.remove) {
					arguments.administrator.removeMapping(virtual=requiredName(arguments.name, "mapping"));
				}
				else {
					arguments.administrator.updateMapping(argumentCollection=arguments.settings);
				}
				break;
			case "datasources":
				if (structKeyExists(arguments.settings, "remove") && arguments.settings.remove) {
					arguments.administrator.removeDatasource(dsn=requiredName(arguments.name, "datasource"));
				}
				else {
					arguments.administrator.updateDatasource(argumentCollection=arguments.settings);
				}
				break;
			case "mail":
				if (structKeyExists(arguments.settings, "removeServer") && arguments.settings.removeServer) {
					arguments.administrator.removeMailServer(
						host=arg(arguments.settings, "host"),
						username=arg(arguments.settings, "username")
					);
				}
				else if (structKeyExists(arguments.settings, "server")) {
					arguments.administrator.updateMailServer(argumentCollection=arguments.settings.server);
				}
				else {
					arguments.administrator.updateMailSetting(
						defaultEncoding=arg(arguments.settings, "defaultEncoding"),
						spoolEnable=arg(arguments.settings, "spoolEnable"),
						timeOut=arg(arguments.settings, "timeOut")
					);
				}
				break;
			case "cache":
				if (structKeyExists(arguments.settings, "removeDefaults") && arguments.settings.removeDefaults) {
					arguments.administrator.removeCacheDefaultConnection();
				}
				else if (structKeyExists(arguments.settings, "defaults")) {
					arguments.administrator.updateCacheDefaultConnection(argumentCollection=arguments.settings.defaults);
				}
				else if (structKeyExists(arguments.settings, "remove") && arguments.settings.remove) {
					arguments.administrator.removeCacheConnection(name=requiredName(arguments.name, "cache"));
				}
				else {
					arguments.administrator.updateCacheConnection(argumentCollection=arguments.settings);
				}
				break;
			case "compiler":
				arguments.administrator.updateCompilerSettings(argumentCollection=arguments.settings);
				break;
			case "performance":
				arguments.administrator.updatePerformanceSettings(
					inspectTemplate=arg(arguments.settings, "inspectTemplate"),
					typeChecking=arg(arguments.settings, "typeChecking")
				);
				break;
			case "component":
				arguments.administrator.updateComponent(argumentCollection=arguments.settings);
				break;
			case "orm":
				arguments.administrator.updateORMSetting(argumentCollection=arguments.settings);
				break;
			case "debug":
				arguments.administrator.updateDebug(argumentCollection=arguments.settings);
				break;
			case "monitoring":
				arguments.administrator.updateMonitoring(monitoring=arguments.settings);
				break;
			case "error":
				arguments.administrator.updateError(
					template500=arg(arguments.settings, "template500"),
					template404=arg(arguments.settings, "template404"),
					statuscode=arg(arguments.settings, "statuscode")
				);
				break;
			case "logging":
				arguments.administrator.updateLogSettings(
					name=requiredName(arguments.name, "logger"),
					level=arg(arguments.settings, "level"),
					appenderClass=arg(arguments.settings, "appenderClass"),
					layoutClass=arg(arguments.settings, "layoutClass"),
					appenderArgs=arg(arguments.settings, "appenderArgs", {}),
					layoutArgs=arg(arguments.settings, "layoutArgs", {})
				);
				break;
			case "regex":
				arguments.administrator.updateRegex(regexType=arg(arguments.settings, "regexType"));
				break;
			case "proxy":
				if (structKeyExists(arguments.settings, "remove") && arguments.settings.remove) {
					arguments.administrator.removeProxy();
				}
				else {
					arguments.administrator.updateProxy(
						proxyEnabled=arg(arguments.settings, "proxyEnabled"),
						proxyServer=arg(arguments.settings, "proxyServer"),
						proxyPort=arg(arguments.settings, "proxyPort"),
						proxyUsername=arg(arguments.settings, "proxyUsername"),
						proxyPassword=arg(arguments.settings, "proxyPassword")
					);
				}
				break;
			case "developmode":
				arguments.administrator.updateDevelopMode(developMode=arg(arguments.settings, "developMode"));
				break;
			case "rest":
				if (structKeyExists(arguments.settings, "remove") && arguments.settings.remove) {
					arguments.administrator.removeRestMapping(virtual=requiredName(arguments.name, "rest mapping"));
				}
				else if (structKeyExists(arguments.settings, "mapping")) {
					arguments.administrator.updateRestMapping(argumentCollection=arguments.settings.mapping);
				}
				else {
					arguments.administrator.updateRestSettings(list=arg(arguments.settings, "list"));
				}
				break;
			case "gateway":
				if (structKeyExists(arguments.settings, "remove") && arguments.settings.remove) {
					arguments.administrator.removeGatewayEntry(id=requiredName(arguments.name, "gateway"));
				}
				else {
					arguments.administrator.updateGatewayEntry(argumentCollection=arguments.settings);
				}
				break;
			case "extensions":
				if (structKeyExists(arguments.settings, "remove") && arguments.settings.remove) {
					arguments.administrator.removeExtension(id=requiredName(arguments.name, "extension"));
				}
				else {
					arguments.administrator.updateExtension(
						id=arg(arguments.settings, "id"),
						version=arg(arguments.settings, "version")
					);
				}
				break;
			case "ai":
				if (structKeyExists(arguments.settings, "remove") && arguments.settings.remove) {
					arguments.administrator.removeAIConnection(name=requiredName(arguments.name, "AI connection"));
				}
				else {
					arguments.administrator.updateAIConnection(
						name=arg(arguments.settings, "name"),
						class=arg(arguments.settings, "class"),
						bundleName=arg(arguments.settings, "bundleName", ""),
						bundleVersion=arg(arguments.settings, "bundleVersion", ""),
						default=arg(arguments.settings, "default", ""),
						custom=arg(arguments.settings, "custom", {})
					);
				}
				break;
			case "remoteclients":
				if (structKeyExists(arguments.settings, "remove") && arguments.settings.remove) {
					arguments.administrator.removeRemoteClient(url=requiredName(arguments.name, "remote client"));
				}
				else if (structKeyExists(arguments.settings, "usage")) {
					if (structKeyExists(arguments.settings, "removeUsage") && arguments.settings.removeUsage) {
						arguments.administrator.removeRemoteClientUsage(code=arg(arguments.settings.usage, "code"));
					}
					else {
						arguments.administrator.updateRemoteClientUsage(
							code=arg(arguments.settings.usage, "code"),
							displayName=arg(arguments.settings.usage, "displayName")
						);
					}
				}
				else {
					arguments.administrator.updateRemoteClient(argumentCollection=arguments.settings);
				}
				break;
			case "security":
				arguments.administrator.updateDefaultSecurityManager(argumentCollection=arguments.settings);
				break;
			case "schedule":
				arguments.administrator.schedule(attributes=arguments.settings);
				break;
			default:
				throw(message="Unknown or unsupported configuration area [#arguments.area#]", type="mcp.tool");
		}
	}

	private any function arg(required struct settings, required string key, any defaultValue=javacast("null", "")) {
		if (structKeyExists(arguments.settings, arguments.key)) {
			return arguments.settings[arguments.key];
		}
		return arguments.defaultValue;
	}

	private string function requiredName(required string name, required string label) {
		if (!len(trim(arguments.name))) {
			throw(message="Missing #arguments.label# name", type="mcp.tool");
		}
		return arguments.name;
	}

}
