<cfset form.name = "lucee_core_dev">
<cftry>
	<cfinsert tablename="LDEV2566_postTable" datasource="ldev2566_POSTGRESQL" formfields="name">
	<cfquery name="getData" datasource="ldev2566_POSTGRESQL">
		select * from LDEV2566_postTable where id = 1
	</cfquery>
	<cfoutput>#getData.name#</cfoutput>
	<cfcatch>
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
</cftry>