/**
 * Base support component for the Lucee configuration MCP server.
 * Handles JSON-RPC 2.0 request reading, response writing and error formatting.
 */
component {

	package static function readRequest() {
		var data = getHTTPRequestData();

		if (data.method != "POST") {
			writeError("", -32600, "Invalid Request: only POST is supported");
			abort;
		}

		if (!structKeyExists(data, "content") || isEmpty(trim(data.content))) {
			writeError("", -32700, "Parse error: empty request body");
			abort;
		}

		try {
			return deserializeJSON(data.content);
		}
		catch (any ex) {
			writeError("", -32700, "Parse error: #ex.message#");
			abort;
		}
	}

	package static function getRpcRequestId(required struct req) {
		return structKeyExists(arguments.req, "id") ? arguments.req.id : "";
	}

	package static function isNotification(required struct req) {
		if (!structKeyExists(arguments.req, "id")) {
			return true;
		}
		return isNull(arguments.req.id);
	}

	package static function writeResult(id, result) {
		setting show = false;
		content type="application/json;charset=UTF-8";
		echo(serializeJSON({
			"jsonrpc": "2.0",
			"id": arguments.id ?: "",
			"result": arguments.result
		}));
	}

	package static function writeError(id, numeric code, string message) {
		setting show = false;
		content type="application/json;charset=UTF-8";
		echo(serializeJSON({
			"jsonrpc": "2.0",
			"id": arguments.id ?: "",
			"error": {
				"code": arguments.code,
				"message": arguments.message
			}
		}));
	}

}
