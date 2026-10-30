<cfscript>
	variables.a[1] = "x";
	writeOutput( ( isArray( variables.a ) ? "array" : "other" ) & "|" & arrayLen( variables.a ) & "|" & variables.a[1] );
</cfscript>
