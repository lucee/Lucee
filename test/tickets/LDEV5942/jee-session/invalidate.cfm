<cfscript>
	result = {
		success: true,
		message: ""
	};

	try {
		// Get the HttpSession before invalidation
		httpSession = getPageContext().getSession();
		result.oldSessionId = httpSession.getId();

		// Invalidate the session
		sessionInvalidate();

		// Try to access the old HttpSession - it should be invalidated
		try {
			// This should throw an exception since the session is invalidated
			httpSession.getAttribute( "test" );
			result.sessionInvalidated = false;
			result.message = "HttpSession was not invalidated - getAttribute() did not throw";
		}
		catch ( any e ) {
			// Expected - session is invalidated
			result.sessionInvalidated = true;
		}
	}
	catch ( any e ) {
		result.success = false;
		result.message = e.message;
	}

	content type="application/json";
	echo( serializeJSON( result ) );
</cfscript>
