<!--- mysql --->
<cfquery name="sessionIndex" >
	SELECT  distinct index_name
	FROM 	information_schema.statistics
	WHERE	TABLE_SCHEMA = 'lucee'
			and table_name='cf_session_data'
</cfquery>
<cfquery name="clientIndex" >
	SELECT  distinct index_name
	FROM 	information_schema.statistics
	WHERE	TABLE_SCHEMA = 'lucee'
			and table_name='cf_client_data'
</cfquery>
<cfoutput>#sessionIndex.RECORDCOUNT#,#clientIndex.RECORDCOUNT#</cfoutput>
	