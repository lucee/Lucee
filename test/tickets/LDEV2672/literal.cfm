<cfscript>
	writeOutput( ( 42 ?: "left hand side was null" ) & "," & ( "true" ?: "default" ) & "," & ( "" ?: "default" ) );
</cfscript>
