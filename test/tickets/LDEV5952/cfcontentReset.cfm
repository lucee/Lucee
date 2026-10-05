<cfscript>
	// Test cfcontent reset=true without prior flush - should work
	writeOutput( "some content that will be cleared" );
	cfcontent( reset=true );
	cfcontent( type="application/json" );
	writeOutput( serializeJSON( { "success": true, "test": "cfcontentReset" } ) );
</cfscript>
