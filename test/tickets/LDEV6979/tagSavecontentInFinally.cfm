<cfset request.ldev6979 = []>
<cftry>
	<cfset arrayAppend( request.ldev6979, "try" )>
	<cfif request.ldev6979Throw><cfthrow message="LDEV-6979 try"></cfif>
	<cfcatch type="Any"><cfset arrayAppend( request.ldev6979, "catch" )></cfcatch>
	<cffinally>
		<cfif true>
			<cfsavecontent variable="request.ldev6979Content">x<cftry>y<cfcatch type="Any"></cfcatch></cftry></cfsavecontent>
			<cfset arrayAppend( request.ldev6979, trim( request.ldev6979Content ) )>
		</cfif>
	</cffinally>
</cftry>
