<cfsilent>
<cfset trace = "">
<cfloop from="1" to="3" index="i">
	<cfset trace = listAppend( trace, i )>
	<cftry>
		<cfif true><cfcontinue></cfif>
		<cfcatch type="any"><cfbreak></cfcatch>
	</cftry>
</cfloop>
<cfset trace2 = "">
<cfset fin = 0>
<cfloop array="#[ 1, 2, 3 ]#" item="item">
	<cfset trace2 = listAppend( trace2, item )>
	<cftry>
		<cfif true><cfcontinue></cfif>
		<cfcatch type="any"><cfbreak></cfcatch>
		<cffinally><cfset fin++></cffinally>
	</cftry>
</cfloop>
</cfsilent><cfoutput>#trace#|#trace2# fin=#fin#</cfoutput>
