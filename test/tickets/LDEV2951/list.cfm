<cfscript>
	// error message of a mistyped cache name
	msg = "";
	try {
		cacheGetAllIds( cacheName = "ldev2951-no-such-cache" );
	}
	catch ( any e ) {
		msg = e.message;
	}

	// the cache can be used by name
	cachePut( id = "ldev2951", value = "app value", cacheName = "ldev2951app" );

	writeOutput( serializeJSON( {
		"message": msg
		, "value": cacheGet( id = "ldev2951", cacheName = "ldev2951app" )
	} ) );
</cfscript>
