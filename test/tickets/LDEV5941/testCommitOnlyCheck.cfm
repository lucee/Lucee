<cfscript>
	sessionCacheKey = uCase( "lucee-storage:session:" & cfid & ":" & application.applicationName );
	storedValue = cacheGet( id=sessionCacheKey, cacheName="ldev5941cache" );

	echo( serializeJSON( {
		sessionValue: session.value,
		cacheLastModifiedAfter: storedValue.lastModified()
	} ) );
</cfscript>
