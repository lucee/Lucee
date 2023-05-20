<!--- mysql --->
<cfquery name="sessionIndex" returntype="array" >
	SELECT  distinct index_name
	FROM 	information_schema.statistics
	WHERE	TABLE_SCHEMA = 'lucee'
			and table_name='cf_session_data'
	ORDER BY index_name
</cfquery>
<cfquery name="clientIndex" returntype="array">
	SELECT  distinct index_name
	FROM 	information_schema.statistics
	WHERE	TABLE_SCHEMA = 'lucee'
			and table_name='cf_client_data'
	ORDER BY index_name
</cfquery>
<cfscript>
	echo([
		session=#sessionIndex#,
		client= #clientIndex#
	].toJson());
</cfscript>
	