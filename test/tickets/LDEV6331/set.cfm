<cfscript>
session.user = url.user ?: "zac";

settings = getApplicationSettings();

result = {
	"action": "set",
	"applicationName": application.applicationName,
	"sessionType": settings.sessionType,
	"sessionStorage": settings.sessionStorage,
	"sessionUser": session.user,
	"cfid": cfid,
	"now": dateTimeFormat( now(), "iso" )
};

content type="application/json";
writeOutput( serializeJson( result ) );
</cfscript>
