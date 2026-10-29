<cfscript>
	s = {};
	s.b[1] = "x";
	writeOutput( ( isStruct( s ) ? "struct" : "other" ) & "|" & ( isArray( s.b ) ? "array" : "other" ) & "|" & s.b[1] );
</cfscript>
