<!--- mssql ---> 
<cfquery name="sessionIndex" returntype="array">
	SELECT 	distinct name
	FROM	sys.indexes
	WHERE	is_hypothetical = 0 
			AND index_id != 0 
			AND object_id = OBJECT_ID('cf_session_data')
</cfquery>
<cfquery name="clientIndex" returntype="array">
	SELECT 	distinct name
	FROM	sys.indexes
	WHERE	is_hypothetical = 0
			AND index_id != 0 
			AND object_id = OBJECT_ID('cf_client_data')
	ORDER BY name
</cfquery>

<cfscript>
	echo([
		session = #sessionIndex#,
		client = #clientIndex#
	].toJson());
</cfscript>