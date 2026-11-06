<cfscript>
	// Report each url and form value with its CFML type so a comma-joined string
	// cannot be mistaken for an array. fieldnames is form bookkeeping, not a field.
	writeOutput( reportScope( "url", url ) );
	writeOutput( reportScope( "form", form, true ) );

	function reportScope( required string scopeName, required struct data, boolean skipFieldNames = false ) {
		var out = "";
		for ( var key in arguments.data ) {
			if ( arguments.skipFieldNames && key == "fieldnames" ) continue;
			out &= reportValue( arguments.scopeName & "|" & key, arguments.data[ key ] );
		}
		return out;
	}

	function reportValue( required string path, required any value ) {
		if ( isArray( arguments.value ) ) {
			return arguments.path & "=array:" & serializeJSON( arguments.value ) & chr( 10 );
		}
		if ( isStruct( arguments.value ) ) {
			var out = "";
			for ( var key in arguments.value ) {
				out &= reportValue( arguments.path & "|" & key, arguments.value[ key ] );
			}
			return out;
		}
		if ( isSimpleValue( arguments.value ) ) {
			return arguments.path & "=simple:" & arguments.value & chr( 10 );
		}
		return arguments.path & "=other:" & serializeJSON( arguments.value ) & chr( 10 );
	}
</cfscript>
