<cfscript>
	param name="url.token";
	param name="url.logType";
	writeLog(text=url.token, log="ldev5756-#url.logType#", level="info");
</cfscript>