<cfscript>
	pc = getPageContext();
	response = pc.getResponse();
	writeOutput( "some content" );
	pc.getOut().flush();
	response.flushBuffer();
	response.resetBuffer();
	cfcontent( type="application/json" );
	writeOutput( serializeJSON( { "success": true, "test": "resetBufferAfterFlush" } ) );
</cfscript>
