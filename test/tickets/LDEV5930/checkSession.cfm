<cfscript>
	// Check if the session data in the DB contains the updated component property values
	// by deserializing the stored data and checking the component's state
	cfidValue = url.cfid ?: "";

	if ( len( cfidValue ) ) {
		qCheck = queryExecute(
			"SELECT * FROM cf_session_data WHERE cfid = :cfid",
			{ cfid: cfidValue }
		);
		sessionExists = qCheck.recordcount gt 0;
		// the data column contains the serialized session data
		// we can't easily inspect it from CFML, but we can check it exists and was updated
		systemOutput( "LDEV-5930 checkSession: cfid=#cfidValue# rows=#qCheck.recordcount#", true );
	}
	else {
		sessionExists = false;
	}

	result = {
		cfidChecked: cfidValue,
		sessionExists: sessionExists,
		recordCount: sessionExists ? qCheck.recordcount : 0
	};

	echo( serializeJSON( result ) );
</cfscript>
