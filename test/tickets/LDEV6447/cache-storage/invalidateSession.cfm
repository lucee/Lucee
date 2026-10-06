<cfscript>
	function storageKey() {
		return "lucee-storage:session:#getPageContext().getCFID()#:#application.applicationName#";
	}

	oldStorageKey = storageKey();
	result = {
		oldSessionId: session.sessionid,
		oldStorageKey: oldStorageKey,
		hadUserId: structKeyExists( session, "user_id" ),
		storedBeforeInvalidate: cacheKeyExists( oldStorageKey, "ldev6447_session_ram" )
	};

	sessionInvalidate();
	result.storedAfterInvalidate = cacheKeyExists( oldStorageKey, "ldev6447_session_ram" );

	// lazily create the replacement session, it is stored at the end of this request
	session.marker = createUUID();
	result.newSessionId = session.sessionid;
	result.newStorageKey = storageKey();
	result.hasUserId = structKeyExists( session, "user_id" );

	echo( serializeJSON( result ) );
</cfscript>
