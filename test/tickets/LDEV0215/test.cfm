<cfparam name="FORM.Scene" default="1">
<cfif FORM.Scene EQ 1>
	<cfquery name="mysqlIndex" datasource="mydatasource">
		SHOW INDEX FROM cf_client_data;
	</cfquery>
	<cfoutput>#mysqlIndex.RECORDCOUNT#</cfoutput>
<cfelse>
	<cfquery name="mssqlIndex" datasource="testdb">
		SELECT * FROM sys.indexes WHERE object_id = (SELECT object_id FROM sys.objects WHERE NAME = 'cf_client_data')
	</cfquery>
	<cfoutput>#mssqlIndex.RECORDCOUNT#</cfoutput>
</cfif>

