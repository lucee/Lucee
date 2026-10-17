component {

	function run() {
		var i = 0;
		var out = "";
		if ( true ) {
			```
			<cfloop condition="i LT 3">
				<cfset i++>
				<cfset out &= i>
			</cfloop>
			```
		}
		return out;
	}

}
