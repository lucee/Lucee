<cfscript>
	result = {
		success: true,
		message: ""
	};

	try {
		// Set some session data before rotation
		session.testValue = "ldev5942_test_data";
		session.testNumber = 42;

		// Get the JSESSIONID before rotation
		httpSession = getPageContext().getSession();
		result.oldSessionId = httpSession.getId();

		// Rotate the session
		sessionRotate();

		// Get the JSESSIONID after rotation
		httpSession = getPageContext().getSession();
		result.newSessionId = httpSession.getId();

		// Check if session data was preserved
		result.dataPreserved = (
			structKeyExists( session, "testValue" )
			&& session.testValue eq "ldev5942_test_data"
			&& structKeyExists( session, "testNumber" )
			&& session.testNumber eq 42
		);
	}
	catch ( any e ) {
		result.success = false;
		result.message = e.message;
	}

	content type="application/json";
	echo( serializeJSON( result ) );
</cfscript>
