<cfoutput>[BEFORE]</cfoutput>
<cfif url.mode eq "forward">
	<cfforward template="/test/tickets/LDEV3298/target.cfm">
<cfelse>
	<cflocation url="/test/tickets/LDEV3298/target.cfm" addtoken="false">
</cfif>
<cfoutput>[AFTER]</cfoutput>
