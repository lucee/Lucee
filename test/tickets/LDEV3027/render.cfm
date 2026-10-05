<cfsetting enablecfoutputonly="true">
<cfscript>
	param name="form.payload";
	param name="form.mode" default="include";
	param name="form.errorTemplate" default="error.cfm";

	if ( form.mode == "request" ) {
		// a request for a missing template whose path carries the payload
		result = _internalRequest(
			template: getDirectoryFromPath( cgi.script_name ) & form.payload & "/index.cfm",
			throwonerror: false
		);
		variables.catch = result.error;
	}
	else {
		try {
			include template="/#form.payload#_LDEV3027_does_not_exist.cfm";
		}
		catch ( any e ) {
			variables.catch = e;
		}
	}
	variables.cfcatch = variables.catch;

	// same as PageContextImpl.handlePageException: catch block in the variables scope, then include the error template
	savecontent variable="html" {
		include template="/lucee/templates/error/#form.errorTemplate#";
	}

	writeOutput( serializeJSON( {
		"type": catch.type,
		"message": catch.message,
		"detail": catch.additional.detail ?: "",
		"html": html
	} ) );
</cfscript>
