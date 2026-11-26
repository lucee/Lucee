<cfset result = "">
<cftry>
	<cfset result &= "t">
	<cfcatch>
		<cfset result &= "c">
	</cfcatch>
	<cffinally>
		<cftry>
			<cfset result &= "f">
			<cfcatch>
				<cfset result &= "x">
			</cfcatch>
		</cftry>
	</cffinally>
</cftry>
