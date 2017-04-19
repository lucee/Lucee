<cftry>
	<cfimport taglib="w1197/lib" prefix="t">
	<cfset Error = "false">
	<cfcatch type="any" >
		<cfset Error = cfcatch.message>
	</cfcatch>
	<cfoutput>#Error#</cfoutput>
</cftry>
