<cfset form.emp_name = "lucee_core">
<cfset form.emp_age = "31">
<cftry>
	<cfinsert tablename="LDEV2566_mysql" datasource="ldev2566_MYSQL" formfields="emp_name,emp_age">
	<cfquery name="getDataOne" datasource="ldev2566_MYSQL">
		select * from LDEV2566_mysql where emp_id = 1
	</cfquery>
	<cfoutput>#getDataOne.emp_name#</cfoutput>
	<cfcatch>
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
</cftry>



