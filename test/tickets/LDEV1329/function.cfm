<cfscript>
	function t() {
		a[1] = "x";
		return ( isArray( a ) ? "array" : "other" ) & "," & ( isArray( variables.a ) ? "array" : "other" ) & "|" & arrayLen( a );
	}
	writeOutput( t() );
</cfscript>
