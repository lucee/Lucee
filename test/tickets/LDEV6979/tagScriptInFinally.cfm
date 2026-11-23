<cfset request.ldev6979 = []>
<cftry>
	<cfset arrayAppend( request.ldev6979, "try" )>
	<cfif request.ldev6979Throw><cfthrow message="LDEV-6979 try"></cfif>
	<cfcatch type="Any"><cfset arrayAppend( request.ldev6979, "catch" )></cfcatch>
	<cffinally>
		<cfif true>
			<cfscript>z = 0; try { arrayAppend( request.ldev6979, "inner" ); } catch ( any e2 ) {}</cfscript>
		</cfif>
	</cffinally>
</cftry>
