<cfscript>
	function nothing2672() { return; }
	a = 1;
	writeOutput( ( ( a + 1 ) ?: "default" ) & "," & ( ucase( "lucee" ) & "!" ?: "default" ) & "," & ( nothing2672() ?: "default" ) );
</cfscript>
