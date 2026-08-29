<cfscript>
	response = getPageContext().getResponse();
	response.reset();
	cfcontent( type="application/json" );
	writeOutput( serializeJSON( { "success": true, "test": "reset" } ) );
</cfscript>
