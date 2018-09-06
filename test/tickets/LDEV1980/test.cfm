<cfscript>
	if(structCount(request.mySQL)) {
		tmpStrt = {};
		tmpStrt.name = "TestDSN";
		tmpStrt.type = "MYSQL";
		tmpStrt.newName = "DsnLDEV1980";
		tmpStrt.host = request.mySQL.server;
		tmpStrt.database = "LDEV1980DB";
		tmpStrt.port = request.mySQL.port;
		tmpStrt.timezone = "";
		tmpStrt.username = request.mySQL.username;
		tmpStrt.password = request.mySQL.password;
		tmpStrt.connectionLimit = "10";
		tmpStrt.connectionTimeout = "0";
		tmpStrt.metaCacheTimeout = "60000";
		tmpStrt.blob = false;
		tmpStrt.clob = false;
		tmpStrt.validate = false;
		tmpStrt.storage = false; // TODO remove all allow functions from Admin.cfc amd this testcase
		tmpStrt.allowedSelect = false;
		tmpStrt.allowedInsert = false;
		tmpStrt.allowedUpdate = false;
		tmpStrt.allowedDelete = false;
		tmpStrt.allowedAlter = false;
		tmpStrt.allowedDrop = false;
		tmpStrt.allowedRevoke = false;
		tmpStrt.allowedCreate = false;
		tmpStrt.allowedGrant = false;
		tmpStrt.verify = false;
		writeoutput(serialiZejson(tmpStrt));
	}
</cfscript>