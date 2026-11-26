<cfset request.ldev6979 = []>
<cftry>
	<cfset arrayAppend( request.ldev6979, "try" )>
	<cfif request.ldev6979Throw><cfthrow message="LDEV-6979 try"></cfif>
	<cfcatch type="Any"><cfset arrayAppend( request.ldev6979, "catch" )></cfcatch>
	<cffinally>
		<cfif true>
			<cfscript>z = 0; arrayAppend( request.ldev6979, "script" );</cfscript>
		</cfif>
	</cffinally>
</cftry>
