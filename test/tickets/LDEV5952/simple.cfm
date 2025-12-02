<cfscript>
	cfcontent( type="application/json" );
	writeOutput( serializeJSON( { "success": true, "test": "simple" } ) );
</cfscript>
