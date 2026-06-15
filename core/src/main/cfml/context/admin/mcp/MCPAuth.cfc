/**
 * Authentication and access control for the configuration MCP server.
 */
component {

	static.ACCESS_NONE = "none";
	static.ACCESS_READ = "read";
	static.ACCESS_WRITE = "write";

	public string function getAccessLevel() {
		var val = "";

		if (structKeyExists(server.system.environment, "LUCEE_ADMIN_MCP_ACCESS")) {
			val = server.system.environment.LUCEE_ADMIN_MCP_ACCESS;
		}
		else if (structKeyExists(server.system.properties, "lucee.admin.mcp.access")) {
			val = server.system.properties["lucee.admin.mcp.access"];
		}

		val = lCase(trim(val ?: static.ACCESS_NONE));

		if (listFindNoCase("none,read,write", val)) {
			return val;
		}

		return static.ACCESS_NONE;
	}

	public boolean function allowsRead(required string accessLevel) {
		return arguments.accessLevel == static.ACCESS_READ || arguments.accessLevel == static.ACCESS_WRITE;
	}

	public boolean function allowsWrite(required string accessLevel) {
		return arguments.accessLevel == static.ACCESS_WRITE;
	}

	public struct function authenticate(required string password) {
		if (!len(trim(arguments.password))) {
			throw(message="Missing administrator password", type="mcp.auth");
		}

		var hashedPassword = "";
		admin action="hashPassword" type="server" pw="#arguments.password#" returnVariable="hashedPassword";

		var adm = createObject("component", "org.lucee.cfml.Administrator").init("server", hashedPassword);
		adm.connect();

		return {
			"administrator": adm,
			"hashedPassword": hashedPassword
		};
	}

}
