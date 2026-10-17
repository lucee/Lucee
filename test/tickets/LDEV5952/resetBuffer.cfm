<cfscript>
	response = getPageContext().getResponse();
	response.resetBuffer();
	cfcontent( type="application/json" );
	writeOutput( serializeJSON( { "success": true, "test": "resetBuffer" } ) );
</cfscript>
