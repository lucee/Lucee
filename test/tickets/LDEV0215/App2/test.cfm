<cfquery name="sessionIndex" >
	SELECT * FROM sys.indexes
	WHERE object_id = (SELECT object_id FROM sys.objects WHERE NAME = 'cf_session_data')
		AND is_unique = 1
</cfquery>
<cfquery name="clientIndex" >
	SELECT * FROM sys.indexes
	WHERE object_id = (SELECT object_id FROM sys.objects WHERE NAME = 'cf_client_data')
		AND is_unique = 1
</cfquery>

<cfif sessionIndex.RECORDCOUNT GT 0 AND clientIndex.RECORDCOUNT GT 0>
	True
<cfelse>
	False
</cfif>
