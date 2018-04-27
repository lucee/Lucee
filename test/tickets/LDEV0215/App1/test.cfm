<cfquery name="sessionIndex" >
	SHOW INDEX FROM cf_session_data;
</cfquery>
<cfquery name="clientIndex" >
	SHOW INDEX FROM cf_client_data;
</cfquery>

<cfif sessionIndex.RECORDCOUNT GT 0 AND clientIndex.RECORDCOUNT GT 0>
	True
<cfelse>
	False
</cfif>