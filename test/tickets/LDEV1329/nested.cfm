<cfscript>
	a[1][2] = "b";
	a[1][1] = "a";
	q[1]["Qty"] = 5;
	writeOutput( ( isArray( a ) ? "array" : "other" ) & "|" & ( isArray( a[1] ) ? "array" : "other" ) & "|" & a[1][1] & "-" & a[1][2]
		& "|" & ( isArray( q ) ? "array" : "other" ) & "|" & ( isStruct( q[1] ) ? "struct" : "other" ) & "|" & q[1].qty );
</cfscript>
