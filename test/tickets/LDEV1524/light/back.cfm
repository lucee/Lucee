<cfscript>
	writeOutput( "a   " & chr( 10 ) & chr( 10 ) & "  b   " & chr( 10 ) & chr( 10 ) );
	setting whitespaceManagement="regular";
	writeOutput( "c   " & chr( 10 ) & chr( 10 ) & "  d" );
</cfscript>