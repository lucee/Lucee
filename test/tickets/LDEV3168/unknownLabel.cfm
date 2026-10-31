<cfscript>
	known: for ( i = 1; i <= 3; i++ ) {
		for ( j = 1; j <= 3; j++ ) {
			break unknown; // no enclosing loop carries this label
		}
	}
</cfscript>
