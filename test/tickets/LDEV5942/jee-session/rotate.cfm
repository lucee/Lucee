<cfscript>
	result = {
		success: true,
		message: ""
	};

	try {
		// Get the JSESSIONID before rotation
		httpSession = getPageContext().getSession();
		result.oldSessionId = httpSession.getId();

		// Rotate the session
		sessionRotate();

		// Get the JSESSIONID after rotation
		httpSession = getPageContext().getSession();
		result.newSessionId = httpSession.getId();
	}
	catch ( any e ) {
		result.success = false;
		result.message = e.message;
	}

	content type="application/json";
	echo( serializeJSON( result ) );
</cfscript>
