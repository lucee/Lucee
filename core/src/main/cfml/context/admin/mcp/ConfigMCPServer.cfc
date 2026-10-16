/**
 * Lucee Configuration MCP Server
 *
 * Endpoint: POST /lucee/admin/mcp.cfm
 * Auth: Authorization: Bearer <Lucee Server Administrator password>
 * Methods: initialize, tools/list, tools/call
 */
component extends="lucee.admin.mcp.MCPSupport" {

	static.PROTOCOL_VERSION = "2024-11-05";
	static.SERVER_NAME = "lucee-config";
	static.SERVER_VERSION = server.lucee.version;

	variables.auth = nullValue();
	variables.util = nullValue();

	public function init() {
		variables.auth = new lucee.admin.mcp.MCPAuth();
		variables.util = new lucee.admin.mcp.MCPUtil();
		variables.tools = {};
		variables.toolsIndex = [];
		variables.accessLevel = variables.auth.getAccessLevel();

		if (!variables.auth.allowsRead(variables.accessLevel)) {
			return this;
		}

		var toolNames = ["ConfigListSections", "ConfigGet", "ConfigUpdate"];

		for (var toolName in toolNames) {
			var tool = createObject("component", "lucee.admin.mcp.tools.#toolName#");
			if (!isObject(tool) || !structKeyExists(tool, "exec") || !structKeyExists(tool, "getName")) {
				continue;
			}
			if (structKeyExists(tool, "init")) {
				tool.init(variables.accessLevel, variables.util);
			}

			if (tool.isWriteTool() && !variables.auth.allowsWrite(variables.accessLevel)) {
				continue;
			}

			variables.tools[tool.getName()] = tool;
			arrayAppend(variables.toolsIndex, {
				"name": tool.getName(),
				"description": tool.getDescription(),
				"inputSchema": tool.getInputSchema()
			});
		}

		return this;
	}

	public function handle() {
		if (!variables.auth.allowsRead(variables.accessLevel)) {
			cfheader(statuscode=503, statustext="Service Unavailable");
			writeError("", -32000, "MCP configuration access is disabled (set LUCEE_ADMIN_MCP_ACCESS=read or write)");
			abort;
		}

		var req = readRequest();
		var requestId = getRpcRequestId(req);

		if (!structKeyExists(req, "method")) {
			if (isNotification(req)) {
				ackNotification();
			}
			writeError(requestId, -32600, "Invalid Request: missing method");
			return;
		}

		var method = req.method ?: "";
		var params = req.params ?: {};

		if (isNotification(req)) {
			ackNotification();
		}

		try {
			if (method == "initialize") {
				handleInitialize(requestId, params);
			}
			else if (method == "tools/list") {
				handleToolsList(requestId);
			}
			else if (method == "tools/call") {
				handleToolsCall(requestId, params);
			}
			else {
				writeError(requestId, -32601, "Method not found: " & method);
			}
		}
		catch (any ex) {
			writeError(requestId, -32603, "Internal error: " & ex.message);
		}
	}

	private function ackNotification() {
		setting show = false;
		cfheader(statusCode=202);
		abort;
	}

	private function handleInitialize(id, params) {
		writeResult(arguments.id, {
			"protocolVersion": static.PROTOCOL_VERSION,
			"serverInfo": {
				"name": static.SERVER_NAME,
				"version": static.SERVER_VERSION
			},
			"capabilities": {
				"tools": [:]
			}
		});
	}

	public function handleToolsList(id) {
		writeResult(arguments.id, {
			"tools": variables.toolsIndex
		});
	}

	private function handleToolsCall(id, params) {
		if (!structKeyExists(params, "name")) {
			writeError(arguments.id, -32602, "Invalid params: missing tool name");
			return;
		}

		var toolName = params.name;
		var args = params.arguments ?: {};
		var tool = variables.tools[toolName] ?: nullValue();

		if (isNull(tool)) {
			writeError(arguments.id, -32602, "Unknown tool: " & toolName);
			return;
		}

		var password = variables.auth.resolvePassword(args);
		if (!len(password)) {
			writeError(arguments.id, -32001, "Unauthorized: missing Authorization header (Bearer <admin password>)");
			return;
		}

		try {
			var auth = variables.auth.authenticate(password);
			structDelete(args, "password");
			writeResult(arguments.id, tool.exec(auth.administrator, args));
		}
		catch (any ex) {
			if (ex.type == "mcp.auth") {
				writeError(arguments.id, -32001, ex.message);
			}
			else {
				writeError(arguments.id, -32603, ex.message);
			}
		}
	}

}
