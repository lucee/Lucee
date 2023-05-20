<!--- mssql ---> 
<cfquery name="sessionIndex" >
	SELECT distinct name FROM sys.indexes
	WHERE object_id = (SELECT object_id FROM sys.objects WHERE NAME = 'cf_session_data')
		AND is_unique = 1
</cfquery>
<cfquery name="clientIndex" >
	SELECT distinct name FROM sys.indexes
	WHERE object_id = (SELECT object_id FROM sys.objects WHERE NAME = 'cf_client_data')
		AND is_unique = 1
</cfquery>
<cfoutput>#sessionIndex.RECORDCOUNT#,#clientIndex.RECORDCOUNT#</cfoutput>