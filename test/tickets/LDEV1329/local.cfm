<cfscript>
	function t() {
		local.a[1] = "x";
		var b[1] = "x";
		return ( isArray( local.a ) ? "array" : "other" ) & "," & ( isArray( b ) ? "array" : "other" ) & "|" & arrayLen( local.a ) & "|" & local.a[1];
	}
	writeOutput( t() );
</cfscript>
