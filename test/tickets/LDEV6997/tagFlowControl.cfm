<cfset result = "">
<cfset n = 0>
<cftry>
	<cfset result &= "t">
	<cfcatch></cfcatch>
	<cffinally>
		<cfloop from="1" to="3" index="i">
			<cftry>
				<cfif i EQ 3><cfbreak></cfif>
				<cfset result &= i>
				<cfcatch></cfcatch>
				<cffinally><cfset result &= "f"></cffinally>
			</cftry>
		</cfloop>
		<cftry>
			<cfset n++>
			<cfif n LT 2><cfthrow message="retry"></cfif>
			<cfset result &= "r">
			<cfcatch><cfretry></cfcatch>
		</cftry>
	</cffinally>
</cftry>
