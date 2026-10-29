<cfscript>
	// every cache the current request can use
	conns = getPageContext().getConfig().getCacheConnections();
	names = [];
	for ( k in conns.keySet() ) names.append( k );
	names.sort( "text" );

	// what the administrator API lists
	admin action="getCacheConnections" type="server" password=server.SERVERADMINPASSWORD returnVariable="adminConns";

	// error message of a mistyped cache name
	msg = "";
	try {
		cacheGetAllIds( cacheName = "ldev2951-no-such-cache" );
	}
	catch ( any e ) {
		msg = e.message;
	}

	sharedTTL = structKeyExists( conns, "ldev2951shared" ) ? conns[ "ldev2951shared" ].getCustom()[ "timeToLiveSeconds" ] : "";

	// the cache can be used by name
	cachePut( id = "ldev2951", value = "app value", cacheName = "ldev2951app" );

	writeOutput( serializeJSON( {
		"names": names
		, "adminNames": valueArray( adminConns.name )
		, "message": msg
		, "sharedTTL": sharedTTL
		, "value": cacheGet( id = "ldev2951", cacheName = "ldev2951app" )
	} ) );
</cfscript>
