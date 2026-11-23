<cfset request.ldev6979 = []>
<cftry>
	<cfset arrayAppend( request.ldev6979, "try" )>
	<cfif request.ldev6979Throw><cfthrow message="LDEV-6979 try"></cfif>
	<cfcatch type="Any"><cfset arrayAppend( request.ldev6979, "catch" )></cfcatch>
	<cffinally>
		<cftry>
			<cfset arrayAppend( request.ldev6979, "inner" )>
			<cfthrow message="LDEV-6979 inner">
			<cfcatch type="Any"><cfset arrayAppend( request.ldev6979, "innercatch" )></cfcatch>
			<cffinally><cfset arrayAppend( request.ldev6979, "innerfinally" )></cffinally>
		</cftry>
	</cffinally>
</cftry>
