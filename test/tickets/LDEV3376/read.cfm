<cfscript>
	result = {};
	try {
		result.exists = fileExists( url.target );
		result.content = fileRead( url.target );
	}
	catch ( any e ) {
		result.error = e.message;
	}
	writeOutput( serializeJSON( result ) );
</cfscript>
