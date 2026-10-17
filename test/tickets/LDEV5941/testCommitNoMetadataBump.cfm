<cfscript>
	// Probe cache.lastvisit BEFORE sessionCommit, then mutate + sessionCommit, then probe again.
	// LDEV-5941 fix: sessionCommit must NOT call touchAfterRequest, so _lastvisit in the persisted
	// scope should stay at the value written by the prior request's natural request-end.
	// Pre-fix behaviour: sessionCommit bumped _lastvisit to this request's timestamp.

	sessionCacheKey = uCase( "lucee-storage:session:" & cfid & ":" & application.applicationName );
	// Map is keyed by lucee.runtime.type.Collection.Key, not String — construct one for lookup.
	lastvisitKey = createObject( "java", "lucee.runtime.type.KeyImpl" ).toKey( "lastvisit" );

	before = cacheGet( id=sessionCacheKey, cacheName="ldev5941cache" );
	lastvisitBefore = before.getValue().get( lastvisitKey ).getValue().getTime();
	lastModifiedBefore = before.lastModified();

	session.value = "midrequest-#getTickCount()#";
	sessionCommit();

	afterStored = cacheGet( id=sessionCacheKey, cacheName="ldev5941cache" );
	lastvisitAfter = afterStored.getValue().get( lastvisitKey ).getValue().getTime();
	lastModifiedAfter = afterStored.lastModified();

	echo( serializeJSON( {
		lastvisitBefore: lastvisitBefore,
		lastvisitAfter: lastvisitAfter,
		lastModifiedBefore: lastModifiedBefore,
		lastModifiedAfter: lastModifiedAfter
	} ) );
</cfscript>
