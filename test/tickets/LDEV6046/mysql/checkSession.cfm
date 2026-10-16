<cfscript>
	// Check if a session exists in the database by querying cf_session_data directly
	// This is the crux of the bug - after sessionInvalidate(), the old session
	// should be DELETED from the database, not just abandoned

	sessionIdToCheck = url.sessionId ?: "";

	if ( len( sessionIdToCheck ) ) {
		// Query the session storage table directly
		// Session ID format is: appName_cfid_cftoken
		cfidValue = listGetAt( sessionIdToCheck, 2, "_" );
		qCheck = queryExecute(
			"SELECT * FROM cf_session_data WHERE cfid = :cfid",
			{ cfid: cfidValue }
		);
		systemOutput(qCheck, true);
		sessionExists = qCheck.recordcount gt 0;
	}
	else {
		sessionExists = false;
	}

	result = {
		sessionIdChecked: sessionIdToCheck,
		sessionExists: sessionExists
	};

	echo( serializeJSON( result ) );
</cfscript>
