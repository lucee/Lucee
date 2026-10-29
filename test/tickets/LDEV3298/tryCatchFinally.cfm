<cfscript>
	echo( "[BEFORE]" );
	try {
		if ( url.mode == "forward" ) cfforward( template="/test/tickets/LDEV3298/target.cfm" );
		else cflocation( url="/test/tickets/LDEV3298/target.cfm", addtoken=false );
		echo( "[IN_TRY_AFTER]" );
	}
	catch ( any e ) {
		echo( "[CAUGHT]" );
	}
	finally {
		echo( "[FINALLY]" );
	}
	echo( "[AFTER]" );
</cfscript>
