<cfscript>
	param name="url.token";
	param name="url.log";
	writeLog(text="testing #url.log# #url.token#", log="#url.log#", level="info");
</cfscript>