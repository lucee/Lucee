component extends="Tool" {

	variables.name = "config_get";
	variables.description = "Read a Lucee configuration section via Administrator.cfc.";
	variables.writeTool = false;

	public function init(string accessLevel="none", any util=nullValue()) {
		super.init(arguments.accessLevel, arguments.util);
		variables.inputSchema = buildInputSchema({
			"area": {
				"type": "string",
				"description": "Configuration section name (use config_list_sections)"
			},
			"name": {
				"type": "string",
				"description": "Optional item name for datasource, mapping, cache, gateway, or AI connection lookups"
			}
		}, ["password", "area"]);
		return this;
	}

	public any function exec(required any administrator, required struct args) {
		var area = lCase(trim(arguments.args.area ?: ""));
		var name = trim(arguments.args.name ?: "");

		if (!len(area)) {
			throw(message="Missing area", type="mcp.tool");
		}

		var result = readArea(arguments.administrator, area, name);
		return serializeResult(result);
	}

	private any function readArea(required any administrator, required string area, string name="") {
		switch (arguments.area) {
			case "info":
				return arguments.administrator.getInfo();
			case "loader":
				return arguments.administrator.getLoaderInfo();
			case "regional":
				return arguments.administrator.getRegional();
			case "charset":
				return arguments.administrator.getCharset();
			case "scope":
				return arguments.administrator.getScope();
			case "application":
				return arguments.administrator.getApplicationSetting();
			case "queue":
				return arguments.administrator.getQueueSetting();
			case "output":
				return arguments.administrator.getOutputSetting();
			case "mappings":
				return arguments.administrator.getMappings();
			case "datasources":
				if (len(arguments.name)) {
					return arguments.administrator.getDatasource(arguments.name);
				}
				return {
					"setting": arguments.administrator.getDatasourceSetting(),
					"datasources": arguments.administrator.getDatasources()
				};
			case "mail":
				return {
					"setting": arguments.administrator.getMailSetting(),
					"servers": arguments.administrator.getMailservers()
				};
			case "cache":
				var cacheResult = {
					"connections": arguments.administrator.getCacheConnections()
				};
				if (len(arguments.name)) {
					cacheResult.connection = arguments.administrator.getCacheConnection(arguments.name);
				}
				return cacheResult;
			case "compiler":
				return arguments.administrator.getCompilerSettings();
			case "performance":
				return arguments.administrator.getPerformanceSettings();
			case "component":
				return {
					"settings": arguments.administrator.getComponent(),
					"mappings": arguments.administrator.getComponentMappings()
				};
			case "orm":
				return {
					"setting": arguments.administrator.getORMSetting(),
					"engine": arguments.administrator.getORMEngine()
				};
			case "debug":
				return {
					"debug": arguments.administrator.getDebug(),
					"setting": arguments.administrator.getDebugSetting(),
					"entries": arguments.administrator.getDebugEntry()
				};
			case "monitoring":
				return arguments.administrator.getMonitoring();
			case "error":
				return arguments.administrator.getError();
			case "logging":
				return {
					"loggers": arguments.administrator.getLogSettings(),
					"mainLog": arguments.administrator.getMainLog()
				};
			case "regex":
				return arguments.administrator.getRegex();
			case "proxy":
				return arguments.administrator.getProxy();
			case "developmode":
				return arguments.administrator.getDevelopMode();
			case "rest":
				return {
					"settings": arguments.administrator.getRestSettings(),
					"mappings": arguments.administrator.getRestMappings()
				};
			case "gateway":
				if (len(arguments.name)) {
					return arguments.administrator.getGatewayEntry(arguments.name);
				}
				return arguments.administrator.getGatewayEntries();
			case "extensions":
				return arguments.administrator.getExtensions();
			case "ai":
				return arguments.administrator.getAIConnections();
			case "remoteclients":
				return arguments.administrator.getRemoteClients();
			case "security":
				return arguments.administrator.getDefaultSecurityManager();
			case "tasks":
				return {
					"setting": arguments.administrator.getTaskSetting(),
					"tasks": arguments.administrator.getTasks()
				};
			default:
				throw(message="Unknown configuration area [#arguments.area#]", type="mcp.tool");
		}
	}

}
