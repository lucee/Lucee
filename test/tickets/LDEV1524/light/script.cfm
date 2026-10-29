<cfscript>
	// line breaks of all kinds, tag split over several writes, number output
	writeOutput( "a  " & chr( 13 ) & chr( 10 ) & chr( 13 ) & chr( 10 ) & "  b" & chr( 13 ) & chr( 10 ) );
	writeOutput( "<p" );
	writeOutput( "re>  x  " & chr( 10 ) & chr( 10 ) & "  y</p" );
	writeOutput( "re>" );
	writeOutput( "  " & chr( 10 ) & "  z  " );
	writeOutput( 42 );
	writeOutput( "  " & chr( 10 ) );
	writeOutput( "<span>" & chr( 160 ) & "</span>" & chr( 10 ) & chr( 10 ) & "end" );
</cfscript>