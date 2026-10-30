<cfscript>
	echo( "[BEFORE]" );
	if ( url.mode == "forward" ) cfforward( template="/test/tickets/LDEV3298/target.cfm" );
	else cflocation( url="/test/tickets/LDEV3298/target.cfm", addtoken=false );
	echo( "[AFTER]" );
</cfscript>
