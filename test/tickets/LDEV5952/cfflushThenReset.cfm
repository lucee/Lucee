<cfscript>
	writeOutput( "some content" );
	cfflush();
	cfcontent( reset=true );
	cfcontent( type="application/json" );
	writeOutput( serializeJSON( { "success": true, "test": "cfflushThenReset" } ) );
</cfscript>
