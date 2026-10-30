<cfscript>
	a = [ 1, 2 ];
	a[3] = 3;
	s = { a: 1 };
	s[ 1 ] = "one";
	writeOutput( ( isArray( a ) ? "array" : "other" ) & "|" & arrayLen( a ) & "|" & ( isStruct( s ) ? "struct" : "other" ) & "|" & listSort( structKeyList( s ), "text" ) );
</cfscript>
