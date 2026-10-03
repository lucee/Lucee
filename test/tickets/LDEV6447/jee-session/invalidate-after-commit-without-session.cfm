<cfscript>
	result = {
		success: true,
		stacktrace: ""
	};

	sessionInvalidate();
	content type="application/json";
	getPageContext().getResponse().flushBuffer();

	try {
		sessionInvalidate();
	}
	catch ( any e ) {
		result.success = false;
		result.stacktrace = e.stacktrace;
	}

	echo( serializeJSON( result ) );
</cfscript>
