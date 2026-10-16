component extends="lucee.admin.mcp.tools.Tool" {

	variables.name = "config_list_sections";
	variables.description = "List Lucee configuration sections available for read and write via MCP.";
	variables.writeTool = false;

	public function init(string accessLevel="none", any util=nullValue()) {
		super.init(arguments.accessLevel, arguments.util);
		variables.inputSchema = buildInputSchema({});
		return this;
	}

	public any function exec(required any administrator, required struct args) {
		var sections = [
			{"area": "info", "read": true, "write": false, "description": "General context information"},
			{"area": "loader", "read": true, "write": false, "description": "Loader version information"},
			{"area": "regional", "read": true, "write": true, "description": "Locale and timezone"},
			{"area": "charset", "read": true, "write": true, "description": "Resource, template and web charsets"},
			{"area": "scope", "read": true, "write": true, "description": "Scope and session settings"},
			{"area": "application", "read": true, "write": true, "description": "Request timeout and script protection"},
			{"area": "queue", "read": true, "write": true, "description": "Concurrent request queue settings"},
			{"area": "output", "read": true, "write": true, "description": "Output writer and compression settings"},
			{"area": "mappings", "read": true, "write": true, "description": "CFML mappings"},
			{"area": "datasources", "read": true, "write": true, "description": "Datasource list and settings"},
			{"area": "mail", "read": true, "write": true, "description": "Mail servers and settings"},
			{"area": "cache", "read": true, "write": true, "description": "Cache connections and defaults"},
			{"area": "compiler", "read": true, "write": true, "description": "Compiler settings"},
			{"area": "performance", "read": true, "write": true, "description": "Template inspection and type checking"},
			{"area": "component", "read": true, "write": true, "description": "Component settings"},
			{"area": "orm", "read": true, "write": true, "description": "ORM settings"},
			{"area": "debug", "read": true, "write": true, "description": "Debugging settings"},
			{"area": "monitoring", "read": true, "write": true, "description": "Monitor display settings"},
			{"area": "error", "read": true, "write": true, "description": "Error templates"},
			{"area": "logging", "read": true, "write": true, "description": "Loggers"},
			{"area": "regex", "read": true, "write": true, "description": "Regex engine"},
			{"area": "proxy", "read": true, "write": true, "description": "HTTP proxy"},
			{"area": "developmode", "read": true, "write": true, "description": "Develop mode"},
			{"area": "rest", "read": true, "write": true, "description": "REST mappings and settings"},
			{"area": "gateway", "read": true, "write": true, "description": "Gateway entries"},
			{"area": "extensions", "read": true, "write": true, "description": "Installed extensions"},
			{"area": "ai", "read": true, "write": true, "description": "AI connections"},
			{"area": "remoteclients", "read": true, "write": true, "description": "Remote client sync definitions"},
			{"area": "security", "read": true, "write": true, "description": "Default security manager"},
			{"area": "schedule", "read": false, "write": true, "description": "Scheduled tasks (write-only via schedule action)"},
			{"area": "tasks", "read": true, "write": false, "description": "Spooler tasks"}
		];

		return serializeResult({
			"access": variables.accessLevel,
			"sections": sections
		});
	}

}
