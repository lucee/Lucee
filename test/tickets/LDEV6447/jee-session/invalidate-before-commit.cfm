<cfscript>
	result = {
		success: true,
		stacktrace: "",
		oldSessionInvalidated: false,
		replacementSessionExists: true,
		sessionCreatedAfterAccess: false,
		oldSessionId: "",
		newSessionId: ""
	};

	try {
		httpSession = getPageContext().getSession();
		result.oldSessionId = httpSession.getId();

		sessionInvalidate();
		result.replacementSessionExists = !isNull( getPageContext().getRequest().getSession( false ) );

		try {
			httpSession.getAttribute( "test" );
		}
		catch ( any e ) {
			result.oldSessionInvalidated = true;
		}

		result.newSessionId = session.sessionid;
		result.sessionCreatedAfterAccess = !isNull( getPageContext().getRequest().getSession( false ) );
	}
	catch ( any e ) {
		result.success = false;
		result.stacktrace = e.stacktrace;
	}

	content type="application/json";
	echo( serializeJSON( result ) );
</cfscript>
