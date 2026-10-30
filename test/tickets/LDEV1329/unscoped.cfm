<cfscript>
	a[1] = "x";
	writeOutput( ( isArray( a ) ? "array" : "other" ) & "|" & arrayLen( a ) & "|" & a[1] );
</cfscript>
