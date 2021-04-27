<cfscript>
	admin
		action="getScope"
		type="server"
		password="#server.SERVERADMINPASSWORD#"
		returnVariable="scope";	

	content type="application/json";
	st = {
		cfidStorage: scope.cfidStorage,
		sessionCFID: session.cfid,
		sessionCFTOKEN: session.cftoken,
		urlCFID: structKeyExists(url, "cfid") ? url.cfid : "",
		urlCFTOKEN: structKeyExists(url, "cftoken") ? url.cftoken : ""
	}
	echo(st.toJson());
</cfscript>