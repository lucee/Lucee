<cfscript>
	// Test form scope parsing with formUrlAsStruct=true (default)

	// Output form keys for validation
	for ( key in form ) {
		if ( key == "fieldnames" ) continue;

		// Handle nested structs
		if ( isStruct( form[ key ] ) ) {
			writeOutput( key & "=" & serializeJSON( form[ key ] ) & chr( 10 ) );
			// Also output nested keys for easy testing
			outputNested( key, form[ key ] );
		}
		// Handle arrays
		else if ( isArray( form[ key ] ) ) {
			writeOutput( key & "=" & arrayToList( form[ key ] ) & chr( 10 ) );
		}
		else {
			writeOutput( key & "=" & form[ key ] & chr( 10 ) );
		}
	}

	function outputNested( prefix, struct data ) {
		for ( k in data ) {
			var fullKey = prefix & "." & k;
			if ( isStruct( data[ k ] ) ) {
				outputNested( fullKey, data[ k ] );
			}
			else if ( isArray( data[ k ] ) ) {
				for ( var i = 1; i <= arrayLen( data[ k ] ); i++ ) {
					if ( isStruct( data[ k ][ i ] ) ) {
						outputNested( fullKey & "[" & ( i - 1 ) & "]", data[ k ][ i ] );
					}
					else {
						writeOutput( fullKey & "[" & ( i - 1 ) & "]=" & data[ k ][ i ] & chr( 10 ) );
					}
				}
			}
			else {
				writeOutput( fullKey & "=" & data[ k ] & chr( 10 ) );
			}
		}
	}
</cfscript>
