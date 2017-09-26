<cfset testQoq = createObject("component", "testQoQ")>
<cfset queryOfQuery = testQoq.getAsset()>
<cftry>
	<cfoutput>
		#queryOfQuery.ASSET_NAME[1]#
	</cfoutput>
	<cfcatch>
		<cfdump var="#cfcatch#" />	
	</cfcatch>
</cftry>