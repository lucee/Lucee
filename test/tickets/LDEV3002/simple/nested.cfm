<cfscript>
	savecontent variable="local.v" {
		writeOutput( "p    q" );
		setting whitespaceManagement=url.mode;
		writeOutput( "r    s" );
	}
	writeOutput( "[" & local.v & "]" );
	writeOutput( "t    u" );
</cfscript>
