<cfscript>
	// check storage before touching the session scope
	result = {
		oldStored: cacheKeyExists( url.oldStorageKey, "ldev6447_session_ram" ),
		newStored: cacheKeyExists( url.newStorageKey, "ldev6447_session_ram" )
	};
	result.sessionId = session.sessionid;
	result.hasUserId = structKeyExists( session, "user_id" );

	echo( serializeJSON( result ) );
</cfscript>
