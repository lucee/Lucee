<cfset request.ldev6979 = []>
<cftry>
	<cfset arrayAppend( request.ldev6979, "try" )>
	<cfif request.ldev6979Throw><cfthrow message="LDEV-6979 try"></cfif>
	<cfcatch type="Any"><cfset arrayAppend( request.ldev6979, "catch" )></cfcatch>
	<cffinally>
		<cfif true>
			<cfset request.ldev6979Count = 0>
			<cftry>
				<cfset request.ldev6979Count++>
				<cfif request.ldev6979Count LT 2><cfthrow message="LDEV-6979 retry"></cfif>
				<cfcatch type="Any"><cfretry></cfcatch>
			</cftry>
			<cfset arrayAppend( request.ldev6979, "retry" & request.ldev6979Count )>
		</cfif>
	</cffinally>
</cftry>
