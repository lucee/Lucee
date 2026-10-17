<cfscript>
	// Test with formUrlAsStruct=false - should preserve literal keys

	for ( key in form ) {
		if ( key == "fieldnames" ) continue;
		writeOutput( key & "=" & form[ key ] & chr( 10 ) );
	}
</cfscript>
