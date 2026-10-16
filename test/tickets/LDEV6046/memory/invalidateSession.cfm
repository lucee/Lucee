<cfscript>
	// Capture old session info before invalidation
	oldSessionId = session.sessionid;
	oldCfid = session.cfid;
	oldCftoken = session.cftoken;

	// This is the function we're testing
	sessionInvalidate();

	// After invalidation, we should have a NEW session
	result = {
		oldSessionId: oldSessionId,
		oldCfid: oldCfid,
		oldCftoken: oldCftoken,
		newSessionId: session.sessionid,
		newCfid: session.cfid,
		newCftoken: session.cftoken,
		hasTestValue: structKeyExists( session, "testValue" ),
		hasUserId: structKeyExists( session, "user_id" )
	};

	echo( serializeJSON( result ) );
</cfscript>
