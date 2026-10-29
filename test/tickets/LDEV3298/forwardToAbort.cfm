<cfscript>
	echo( "[BEFORE]" );
	cfforward( template="/test/tickets/LDEV3298/abortTarget.cfm" );
	echo( "[AFTER]" );
</cfscript>
