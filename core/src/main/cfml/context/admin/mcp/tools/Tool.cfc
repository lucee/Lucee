abstract component {

	variables.accessLevel = "none";

	public function init(string accessLevel="none", any util=nullValue()) {
		variables.accessLevel = arguments.accessLevel;
		if (!isNull(arguments.util)) {
			variables.util = arguments.util;
		}
		return this;
	}

	public string function getName() {
		return variables.name;
	}

	public string function getDescription() {
		return variables.description;
	}

	public struct function getInputSchema() {
		return variables.inputSchema;
	}

	public boolean function isWriteTool() {
		return variables.writeTool ?: false;
	}

	public any function exec(required any administrator, required struct args) {
		throw(message="Tool not implemented", type="mcp.tool");
	}

	static function toTextContent(required string text) {
		return {
			"content": [
				{
					"type": "text",
					"text": arguments.text
				}
			]
		};
	}

	protected struct function passwordSchema() {
		return {
			"password": {
				"type": "string",
				"description": "Lucee Server Administrator password"
			}
		};
	}

	protected struct function buildInputSchema(required struct extraProperties, array required=[]) {
		var schema = {
			"type": "object",
			"properties": duplicate(passwordSchema())
		};
		structAppend(schema.properties, arguments.extraProperties);
		if (!arrayLen(arguments.required)) {
			schema.required = ["password"];
		}
		else {
			schema.required = arguments.required;
		}
		return schema;
	}

	protected any function serializeResult(required any value) {
		if (!structKeyExists(variables, "util") || isNull(variables.util)) {
			throw(message="MCP util not initialized", type="mcp.tool");
		}
		return static.toTextContent(variables.util.toJsonText(arguments.value));
	}

}
