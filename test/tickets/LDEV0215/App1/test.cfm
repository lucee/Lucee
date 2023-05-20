<cfquery name="sessionIndex" >
	SHOW INDEX FROM cf_session_data;
</cfquery>
<cfquery name="clientIndex" >
	SHOW INDEX FROM cf_client_data;
</cfquery>

<cfoutput>#sessionIndex.RECORDCOUNT#,#clientIndex.RECORDCOUNT#</cfoutput>
