<cfset value = 50>
<cfif value GT 100>
	<cfset result = "high">
<cfelseif value GT 50>
	<cfset result = "medium">
<cfelse>
	<cfset result = "low">
</cfif>
<cfoutput>#result#</cfoutput>
