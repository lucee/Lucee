<cfscript>
	savecontent variable="local.v" {
		writeOutput( "  x    y  " );
	}
	writeOutput( "[" & local.v & "]" );
	setting whitespaceManagement=url.mode;
	savecontent variable="local.w" {
		writeOutput( "  x    y  " );
	}
	writeOutput( "[" & local.w & "]" );
</cfscript>
