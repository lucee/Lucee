<cfscript>
	// Mutate, sessionCommit, then NO further mutations.
	// Capture the cache entry's lastModified after sessionCommit.
	// Request end runs after this template returns — if no double-write happens,
	// the cache entry's lastModified stays at this captured value.

	session.value = "committed-at-#getTickCount()#";
	sessionCommit();

	sessionCacheKey = uCase( "lucee-storage:session:" & cfid & ":" & application.applicationName );
	// IKStorageValue exposes lastModified directly via a public method.
	storedValue = cacheGet( id=sessionCacheKey, cacheName="ldev5941cache" );

	echo( serializeJSON( {
		sessionValue: session.value,
		cacheLastModifiedAtCommit: storedValue.lastModified()
	} ) );
</cfscript>
