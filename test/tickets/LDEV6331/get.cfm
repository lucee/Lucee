<cfscript>
// Probes whether the persisted session entry is still alive in its backend.
//   cache backends (ram/redis): lucee-storage:<scope>:<cfid>:<appName> key in the cache
//   datasource backend:         cf_session_data row with expires > now()
sessionInStorage = false;
if ( url.sessionStorage eq "datasource" ) {
	// expires column is epoch millis
	query name="q" datasource="#getApplicationSettings().dataSource#" {
		echo( "SELECT expires FROM cf_session_data WHERE cfid = " );
		queryParam value=cfid sqltype="varchar";
		echo( " AND name = " );
		queryParam value=application.applicationName sqltype="varchar";
	}
	sessionInStorage = q.recordcount && q.expires[ 1 ] > dateTimeFormat( now(), "epochms" );
} else {
	// IKHandlerCache builds storage keys as UCase("lucee-storage:<scope>:<cfid>:<appName>")
	sessionCacheKey = uCase( "lucee-storage:session:" & cfid & ":" & application.applicationName );
	sessionInStorage = !isNull( cacheGet( id=sessionCacheKey, cacheName="ldev6331cache" ) );
}

result = {
	"action": "get",
	"applicationName": application.applicationName,
	"sessionStorage": getApplicationSettings().sessionStorage,
	"sessionUser": session?.user ?: "(undefined)",
	"cfid": cfid,
	"sessionInStorage": sessionInStorage,
	"now": dateTimeFormat( now(), "iso" )
};

content type="application/json";
writeOutput( serializeJson( result ) );
</cfscript>
