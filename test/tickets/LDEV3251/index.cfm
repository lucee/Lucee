<cfscript>
	try {
		if ( url.keyExists( "settings" ) ) {
			echo( serializeJSON( getApplicationSettings().serialization ) );
		}
		else {
			echo( serializeJSON( { "d": createDateTime( 2021, 1, 29, 11, 45, 26, 123 ) } ) );
		}
	}
	catch ( any e ) {
		echo( "ERROR: " & e.message );
	}
</cfscript>
