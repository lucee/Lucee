<cfscript>
	writeOutput( "a   " & chr( 10 ) & chr( 10 ) & "  b" );
	flush;
	writeOutput( "   " & chr( 10 ) & chr( 10 ) & "  c   " & chr( 10 ) & chr( 10 ) & "d" );
</cfscript>