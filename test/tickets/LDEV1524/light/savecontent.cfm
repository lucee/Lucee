<cfscript>
	savecontent variable="local.v" {
		writeOutput( "  x    y  " );
	}
	writeOutput( "[" & local.v & "]" );
</cfscript>