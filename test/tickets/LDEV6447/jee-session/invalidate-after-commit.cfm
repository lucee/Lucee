<cfscript>
	result = {
		success: true,
		stacktrace: "",
		sessionInvalidated: false
	};

	httpSession = getPageContext().getSession();
	content type="application/json";
	getPageContext().getResponse().flushBuffer();

	try {
		sessionInvalidate();
		try {
			httpSession.getAttribute( "test" );
		}
		catch ( any e ) {
			result.sessionInvalidated = true;
		}
	}
	catch ( any e ) {
		result.success = false;
		result.stacktrace = e.stacktrace;
	}

	echo( serializeJSON( result ) );
</cfscript>
