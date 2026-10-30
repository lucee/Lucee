<cfscript>
	function leave( mode ) {
		echo( "[IN_FUNCTION]" );
		if ( mode == "forward" ) cfforward( template="/test/tickets/LDEV3298/target.cfm" );
		else cflocation( url="/test/tickets/LDEV3298/target.cfm", addtoken=false );
		echo( "[IN_FUNCTION_AFTER]" );
	}
	echo( "[BEFORE]" );
	leave( url.mode );
	echo( "[AFTER]" );
</cfscript>
