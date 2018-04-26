<cfparam name="FORM.Scene" default="1">
<cfif FORM.Scene EQ 1>
	<cfquery name="mysqlIndex" datasource="my-ldev-215">
		SHOW INDEX FROM cf_client_data;
	</cfquery>
	<cfoutput>#mysqlIndex.RECORDCOUNT#</cfoutput>
<cfelse>
	<cfquery name="mssqlIndex" datasource="ms-ldev-215">
		SELECT * FROM sys.indexes
		WHERE object_id = (SELECT object_id FROM sys.objects WHERE NAME = 'cf_session_data')
			AND is_unique = 1
	</cfquery>
	<cfoutput>#mssqlIndex.RECORDCOUNT#</cfoutput>
</cfif>