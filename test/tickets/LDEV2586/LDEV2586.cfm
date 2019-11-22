<cfquery name="test" datasource="LDEV2586_DSN">
   SELECT * FROM LDEV2586
</cfquery>

<cfparam name="form.scene" default="">
<cfif form.scene eq 1>
	<cfquery name="subUsers" datasource="LDEV2586_DSN">
	   SELECT * FROM LDEV2586 WHERE value = <CFQUERYPARAM value='#test.value[1]#' cfsqltype="CF_SQL_DECIMAL" maxlength="8">
	</cfquery>
	<cfoutput>#subUsers.value#</cfoutput>
<cfelseif form.scene eq 2>
	<cfquery name="subUsers" datasource="LDEV2586_DSN">
	   SELECT * FROM LDEV2586 WHERE value = <CFQUERYPARAM value='#test.value[1]#' cfsqltype="CF_SQL_DECIMAL" maxlength="7">
	</cfquery>
	<cfoutput>#subUsers.value#</cfoutput>
<cfelseif form.scene eq 3>
	<cfquery name="subUsers" datasource="LDEV2586_DSN">
	   SELECT * FROM LDEV2586 WHERE value = <CFQUERYPARAM value='#test.value[2]#' cfsqltype="CF_SQL_DECIMAL" maxlength="5">
	</cfquery>
	<cfoutput>#subUsers.value#</cfoutput>
</cfif>