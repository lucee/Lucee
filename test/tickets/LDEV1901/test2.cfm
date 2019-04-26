<cftry>
	<cfsearch name="srchName" collection="collectionName" criteria="TestWord^s">
	<cfset result = isQuery(srchName)>
	<cfcatch type="any">
		<cfset result ="#cfcatch.message#" />
	</cfcatch>
	<cffinally> 
		<cfcollection action= "Delete" collection="collectionName" language="">
	</cffinally> 
</cftry>
<cfoutput>#result#</cfoutput>
