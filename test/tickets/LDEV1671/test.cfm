<cftry>
	<cfset hasError = false>
	<cfquery name="test">
		SELECT *
		FROM pages
		WHERE page_id NOT IN (<cfqueryparam cfsqltype="CF_SQL_INTEGER" value="" list="true">)
		LIMIT 100
	</cfquery>
	<cfcatch type="any">
		<cfset hasError = true>
	</cfcatch>
</cftry>

<cfoutput>#hasError#</cfoutput>