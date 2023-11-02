<cfset form.name = "lucee">
<cfset form.age = "4">
<cftry>
	<cfinsert tablename="LDEV2566_mssql" datasource="ldev2566_MSSQL" formfields="name,age">
	<cfquery name="getData" datasource="ldev2566_MSSQL">
		select * from LDEV2566_mssql where id = 1
	</cfquery>
	<cfoutput>#getData.name#</cfoutput>
	<cfcatch>
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
</cftry>



