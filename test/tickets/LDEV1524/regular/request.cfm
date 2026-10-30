<cfscript>
	writeOutput( "a   " & chr( 10 ) & chr( 10 ) & "  b" );
	setting whitespaceManagement=url.mode;
	writeOutput( chr( 10 ) & chr( 10 ) & "  c   " & chr( 10 ) & chr( 10 ) & "  d" );
</cfscript>