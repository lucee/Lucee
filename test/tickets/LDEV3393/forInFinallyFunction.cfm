<cfscript>
	function structKeys( struct data ) {
		var res = [];
		try {
			res.append( "try" );
		}
		finally {
			for ( var key in data ) {
				res.append( key );
			}
		}
		return res;
	}

	function arrayItems( array data ) {
		var res = [];
		try {
			res.append( "try" );
		}
		finally {
			for ( var loopItem in data ) {
				res.append( loopItem );
			}
		}
		return res;
	}

	writeOutput( serializeJSON( { "struct": structKeys( [ "key1": "foo", "key2": "bar" ] ), "array": arrayItems( [ "a", "b" ] ) } ) );
</cfscript>
