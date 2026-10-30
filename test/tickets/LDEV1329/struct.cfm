<cfscript>
	a["1"] = "x";
	b = {};
	b[1] = "y";
	c.d = {};
	c.d[1] = "z";
	c.e["1"] = "z";
	writeOutput( ( isStruct( a ) ? "struct" : "other" ) & "|" & ( isStruct( b ) ? "struct" : "other" ) & "|" & ( isStruct( c.d ) ? "struct" : "other" )
		& "|" & a["1"] & "|" & b["1"] & "|" & c.d["1"] );
</cfscript>
