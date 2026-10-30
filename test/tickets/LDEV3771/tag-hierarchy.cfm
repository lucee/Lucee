<cftry>
	<cfset sct = {}>
	<cfset x = sct.missing>
	<cfcatch type="database" | "expression" name="ex">
		<cfoutput>caught:#ex.type#</cfoutput>
	</cfcatch>
</cftry>
