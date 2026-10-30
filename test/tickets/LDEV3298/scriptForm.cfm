<cfscript>
	echo( "[BEFORE]" );
	if ( url.mode == "forward" ) {
		forward template="/test/tickets/LDEV3298/target.cfm";
	}
	else {
		location url="/test/tickets/LDEV3298/target.cfm" addtoken=false;
	}
	echo( "[AFTER]" );
</cfscript>
