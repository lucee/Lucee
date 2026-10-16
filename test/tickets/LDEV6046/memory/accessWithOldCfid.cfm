<cfscript>
	// This simulates another browser/request trying to access with the OLD cfid/cftoken
	// After sessionInvalidate(), this should NOT return a valid session with user_id

	result = {
		sessionId: session.sessionid,
		hasUserId: structKeyExists( session, "user_id" ),
		userId: session.user_id ?: "",
		hasTestValue: structKeyExists( session, "testValue" ),
		testValue: session.testValue ?: ""
	};

	echo( serializeJSON( result ) );
</cfscript>
