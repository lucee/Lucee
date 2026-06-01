<cfscript>
// Deliberately read-only. Reads bump _lastvisit via direct data0.put() which
// bypasses hasChanges() — so the store path never re-fires cache.put() and the
// cache TTL ticks down uninterrupted from the last real write.
result = {
	"action": "get",
	"applicationName": application.applicationName,
	"sessionStorage": getApplicationSettings().sessionStorage,
	"sessionUser": session?.user ?: "(undefined)",
	"cfid": cfid,
	"now": dateTimeFormat( now(), "iso" )
};

content type="application/json";
writeOutput( serializeJson( result ) );
</cfscript>
