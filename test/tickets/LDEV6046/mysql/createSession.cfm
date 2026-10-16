<cfscript>
	// Store test value if provided
	if ( structKeyExists( url, "testValue" ) ) {
		session.testValue = url.testValue;
	}

	// Set a marker to prove session is active
	session.user_id = createUUID();

	result = {
		sessionId: session.sessionid,
		cfid: session.cfid,
		cftoken: session.cftoken,
		user_id: session.user_id,
		testValue: session.testValue ?: ""
	};

	echo( serializeJSON( result ) );
</cfscript>
